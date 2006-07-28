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
 * Also takes care of calling {@link CapselaInternalFrame#frameClosed()} when
 * the frame has been closed.
 * 
 * Note that this listener will be removed from the internal frame's list of
 * listeners at the first call of either
 * {@link #internalFrameClosing(InternalFrameEvent)}
 * or {@link #internalFrameClosed(InternalFrameEvent)}.
 *
 * @author Dean Mao
 * @author Nathan Rose
 * @created July 31, 2003
 */
public class ClosingFrameListener implements InternalFrameListener {
    private static final Log LOG = LogFactory.getLog( ClosingFrameListener.class );

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
        performClosingOperations();
    }
    
    private final void performClosingOperations() {
        // called from two places since internalFrameClosing() is not necessarily always called
        // (such as when calling dispose() on the frame)
        // the check for null shouldn't be necessary, since we remove ourself as a listener...
        if( _frame != null ) {
            WorkflowEditor.getInstance().removeInternalFrameMenuItem( _frame );
            LOG.debug( "ClosingFrameListener.internalFrameClosing frame=" + _frame.getClass().getName() );
            _frame.removeInternalFrameListener( this );
            try {
                _frame.frameClosed();
            }
            catch( Exception e ) {
                LOG.warn( "Error closing frame!", e );
            }
            _frame = null;
        }
    }

    /**
     * @see InternalFrameListener#internalFrameActivated(InternalFrameEvent)
     */
    public void internalFrameActivated( InternalFrameEvent e ) {
        WorkflowEditor.getInstance().setSelectedInternalFrameMenuItem( _frame );
    }

    /**
     * @see InternalFrameListener#internalFrameClosed(InternalFrameEvent)
     */
    public void internalFrameClosed( InternalFrameEvent e ) {
        performClosingOperations();
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
        WorkflowEditor.getInstance().addInternalFrameMenuItem( _frame );
    }
}
