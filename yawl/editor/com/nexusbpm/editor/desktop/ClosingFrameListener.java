package com.nexusbpm.editor.desktop;

import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.WorkflowEditor;


/**
 * This is just a frame listener that listens for closing window events. When we
 * close a window we want to remove it from the list of open windows menu on the
 * capsela client.
 *
 * @author Dean Mao
 * @created July 31, 2003
 */
public class ClosingFrameListener implements InternalFrameListener {

	private static final Log LOG = LogFactory.getLog( ClosingFrameListener.class );


	/**
	 * Sometimes we lose track that we need closing.  we always close from internalFrameClosing, but using this
	 * boolean we might close from internalFrameClosed or finalize
	 */
	private boolean needsClosing = false;

	private CapselaInternalFrame _frame;

	/**
	 * Creates a closing frame listener that is listening to events for the
	 * given frame (note that this constructor does not add the created
	 * listener as a listener to the given frame).
	 * @param frame the frame that this listener will listen to.
	 */
	public ClosingFrameListener( CapselaInternalFrame frame ) {
		_frame = frame;
	}

	/**
	 * @see InternalFrameListener#internalFrameClosing(InternalFrameEvent)
	 */
	public void internalFrameClosing( InternalFrameEvent e ) {
		if( LOG.isDebugEnabled() ) {
			LOG.debug( "ClosingFrameListener.internalFrameClosing frame=" + _frame.getClass().getName() );
		}
		closing();
	}

	/**
	 * close stuff that needs closing
	 */
	private void closing() {
		LOG.debug( "ClosingFrameListener.closing" );

		if( null != _frame ) {
//			WorkflowEditor.getInstance().removeWindowMenuItem( _frame );
			needsClosing = false;
			_frame.removeEditorFrameListeners();
			try {
				_frame.clear();
			}
			catch( Throwable t ) {
				LOG.warn( t );
			}
		}
	}

	/**
	 * @see InternalFrameListener#internalFrameActivated(InternalFrameEvent)
	 */
	public void internalFrameActivated( InternalFrameEvent e ) {
//		WorkflowEditor.getInstance().setSelectedInternalFrame( _frame );
	}

	/**
	 * @see InternalFrameListener#internalFrameClosed(InternalFrameEvent)
	 */
	public void internalFrameClosed( InternalFrameEvent e ) {
		LOG.debug( "ClosingFrameListener.internalFrameClosed - setting _frame to null" );

		if( needsClosing ) {
			closing();
		}
		_frame = null;
	}

	/**
	 * Empty implementation.
	 * @see InternalFrameListener#internalFrameDeactivated(javax.swing.event.InternalFrameEvent)
	 */
	public void internalFrameDeactivated( InternalFrameEvent e ) {
		// Empty.
	}

	/**
	 * Empty implementation.
	 * @see InternalFrameListener#internalFrameDeiconified(InternalFrameEvent)
	 */
	public void internalFrameDeiconified( InternalFrameEvent e ) {
		// Empty.
	}

	/**
	 * Empty implementation.
	 * @see InternalFrameListener#internalFrameIconified(InternalFrameEvent)
	 */
	public void internalFrameIconified( InternalFrameEvent e ) {
		// Empty.
	}

	/**
	 * Empty implementation.
	 * @see InternalFrameListener#internalFrameOpened(InternalFrameEvent)
	 */
	public void internalFrameOpened( InternalFrameEvent e ) {
//		WorkflowEditor.getInstance().addWindowMenuItem( _frame );
		needsClosing = true;
		throw new RuntimeException("implement for yawl context");
	}

	/**
	 * @see Object#finalize()
	 */
	public void finalize() throws Throwable {
		LOG.debug( "ClosingFrameListener.finalize" );
		if( needsClosing ) {
			closing();
		}
		_frame = null;
		super.finalize();
	}
}
