package com.nexusbpm.editor.persistence;

import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.event.InternalFrameListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.data.YVariable;

import com.nexusbpm.NexusWorkflow;
import com.nexusbpm.editor.desktop.CapselaInternalFrame;
import com.nexusbpm.editor.desktop.ComponentEditorFrameListener;
import com.nexusbpm.editor.editors.ComponentEditor;
import com.nexusbpm.editor.editors.NetEditor;
import com.nexusbpm.editor.editors.net.cells.GraphEdge;
import com.nexusbpm.editor.editors.net.cells.GraphPort;
import com.nexusbpm.editor.editors.net.cells.NexusCell;
import com.nexusbpm.editor.icon.AnimatedIcon;
import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.icon.RenderingHints;
import com.nexusbpm.editor.tree.SharedNode;
import com.nexusbpm.services.NexusServiceInfo;

public class EditorDataProxy<Type> extends au.edu.qut.yawl.persistence.managed.DataProxy<Type> implements Transferable {


	/** A <tt>graph cell</tt> is the displayed JGraph object for a component. */
	private NexusCell _graphCell;

	/**
	 * @return the graph cell for this component.
	 */
	public NexusCell getGraphCell() {
		return _graphCell;
	}

	/**
	 * Sets the graph cell for this component.
	 * @param cell the graph cell to set.
	 */
	public void setGraphCell( NexusCell cell ) {
		_graphCell = cell;
		_graphCell.setProxy( this );
	}
	
	private SharedNode _treeNode;
	
	public void setTreeNode(SharedNode node) {
		_treeNode = node;
	}
	
	public SharedNode getTreeNode() {
		return _treeNode;
	}


	private GraphEdge _graphEdge;

	/**
	 * Gets the graph edge associated with this dependent controller. Used for
	 * controllers for control edges and data edges.
	 * @return the graph edge associated with the dependent controller.
	 */
	public GraphEdge getGraphEdge() {
		return _graphEdge;
	}

	/**
	 * Sets the graph edge associated with this dependent controller. Used for
	 * controllers for control edges and data edges.
	 * @param edge the graph edge to set.
	 */
	public void setGraphEdge( GraphEdge edge ) {
		_graphEdge = edge;
		_graphEdge.setProxy( this );
	}

	/** The <tt>port</tt> on the JGraph where edges are connected. */
	private GraphPort _graphPort;

	/**
	 * @return the graph port for this component's JGraph object.
	 */
	public GraphPort getGraphPort() {
		return _graphPort;
	}

	/**
	 * Sets the port used for this component on the graph.
	 * @param port the port to use for this component.
	 */
	public void setGraphPort( GraphPort port ) {
		_graphPort = port;
	}

	/**
	 * Gets the object displayed on the JGraph for this controller's independent
	 * domain object.
	 * @return the JGraph object for this controller's independent domain
	 *         object.
	 */
	public Object getJGraphObject() {
		return getGraphCell();
	}
	
	/** The data flavor we'll be using for any drag/drop operations. */
	public final static DataFlavor PROXY_FLAVOR = new DataFlavor( EditorDataProxy.class, "proxy flavor" );

	/**
	 * The data flavors in which data can be transferred
	 * @see java.awt.datatransfer.Transferable#getTransferDataFlavors()
	 */
	static DataFlavor flavors[] = { PROXY_FLAVOR };
	
	public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
		if( flavor.equals( PROXY_FLAVOR ) ) { return this; }
		throw new UnsupportedFlavorException( flavor );
	}

	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	public boolean isDataFlavorSupported( DataFlavor flavor ) {
		return flavor.equals( PROXY_FLAVOR );
	}
	
//	/**
//	 * Gets the small icon for the domain object specified by this controller.
//	 * @return the small icon for this controller's domain object.
//	 */
//	public ImageIcon iconSmall() {
//		return ApplicationIcon.getIcon( getData().getClass().getName(), RenderingHints.ICON_SMALL );
//	}

	/**
	 * Gets the icon for the domain object specified by this controller.
	 * @return the icon for this controller's domain object.
	 */
	public ImageIcon icon() {
		String iconName = getIconName();
		try {
			return ApplicationIcon.getIcon(iconName, RenderingHints.ICON_MEDIUM );
		}
		catch(Error e) {
			return ApplicationIcon.getIcon("Component", RenderingHints.ICON_MEDIUM );
		}
	}
    
    public ImageIcon iconSmall() {
        String iconName = getIconName();
        try {
            return ApplicationIcon.getIcon(iconName, RenderingHints.ICON_SMALL );
        }
        catch(Error e) {
            return ApplicationIcon.getIcon("Component", RenderingHints.ICON_SMALL );
        }
    }
    
    private String getIconName() {
        String iconName = "Component";
		if (getData() instanceof YAtomicTask) {
			YAtomicTask task = (YAtomicTask) getData();
			String serviceName = task.getID() 
				+ NexusWorkflow.NAME_SEPARATOR 
				+ NexusWorkflow.SERVICENAME_VAR;
			String value = null;
			if (task.getParent() != null) {
				YVariable var = task.getParent().getLocalVariable(serviceName);
				if (var != null) {
					value = var.getInitialValue();
				}
				if (value != null) iconName = value;
			}
		}
		else {
			iconName = getData().getClass().getName();
		}
        return iconName;
    }

	private AnimatedIcon _animatedIcon = null;

	private Container _iconContainer;

	/**
	 * Gets the animated icon for this controller's component.
	 * @param container the container that the icon is displayed in.
	 * @return the animated icon for this controller's component.
	 */
	public AnimatedIcon iconAnimated( Container container ) {
		if( _animatedIcon == null ) {
			_animatedIcon = ApplicationIcon.getAnimatedRotatingIcon( getData().getClass().getName(), 20 );
		}
		_iconContainer = container;
		return _animatedIcon;
	}

	/**
	 * Stops this controller's component's animated icon, removes it from the
	 * container it is displayed in, and nulls the variable in the controller.
	 */
	public synchronized void clearAnimatedIcon() {
		if( _animatedIcon != null ) {
			_animatedIcon.stop();
			_iconContainer.remove( _animatedIcon );
			_iconContainer.repaint();
			_animatedIcon = null;
			_iconContainer = null;
		}
	}

	/**
	 * Retrieves the internal frame for the editor corresponding to this node's
	 * component. If the editor has not yet been created it gets created then
	 * returned.
	 * @return the frame of the editor for this node's component.
	 * @throws CapselaException not thrown in the code.
	 * @see #getEditor()
	 */
	
	public CapselaInternalFrame getEditor() throws Exception {
			if( getData() instanceof YNet ) {
				_editor = new NetEditor();
			}
			else {
				Class editorClass = getEditorClass();
				if (editorClass != null) {
					_editor = (ComponentEditor) editorClass.newInstance();
				}
			}
//			_editor.setProxy(this);
//			_editor.initializeUI();
			if (_editor != null) {
				_editor.setTitle( this.getLabel() );
				LOG.debug( "removing frame listeners" );
				removeEditorFrameListeners();
				LOG.debug( "adding frame listener for editor: " + _editor.getClass().getName() );
				_editor.addInternalFrameListener( getInternalFrameListener( _editor ) );
			}
			return _editor;
	}

	/**
	 * uses NexusServiceInfo to retrieve the appropriate editor class for
	 * the underlying YAWL object.
	 * 
	 * @return the class of the editor
	 */
	public Class getEditorClass() {
		Class retval = null;
		if (getData() instanceof YNet) {
			retval = NetEditor.class;
		} else if (getData() instanceof YAtomicTask) {
			YAtomicTask task = (YAtomicTask) getData();
			YVariable var = task.getParent().getLocalVariable(
					task.getID() + NexusWorkflow.NAME_SEPARATOR
							+ NexusWorkflow.SERVICENAME_VAR);
			if (var != null) {
				NexusServiceInfo service = NexusServiceInfo
						.getServiceWithName(var.getInitialValue());
				try {
					retval = Class.forName(service.getEditorClassName());
				} catch (ClassNotFoundException e) {
					retval = null;
				}
			}
		} else
			retval = null;
		return retval;
	}

	
	
	/**
	 * This node's component editor.
	 * 
	 * @see ComponentNode#getEditor()
	 */
	private ComponentEditor _editor;

	/**
	 * Our frame listener.
	 */
	private InternalFrameListener _frameListener = null;

	private static final Log LOG = LogFactory.getLog( EditorDataProxy.class );
	/**
	 * Creates and returns the appropriate InternalFrameListener for the given
	 * editor for this node's component based on whether this node's component
	 * is an instance or not and what kind of component it is.
	 * @param editor the editor for this node's component.
	 * @return the appropriate InternalFrameListener.
	 * @throws CapselaException if there is an error retrieving this node's
	 *                          component's "is instance" attribute (shouldn't
	 *                          happen).
	 */
	public InternalFrameListener getInternalFrameListener( ComponentEditor editor ) throws Exception {
		Class editorClass = getEditorClass();
//		if( ( (Boolean) _controller.getDomainObject().getAttribute( Component.ATTR_INSTANCE ) ).booleanValue() ) {
//			_frameListener = new InstanceEditorFrameListener( this, editor );
//		}
//		else 
//		if( editorClass.equals( ConditionalEditor.class ) || editorClass.equals( AliasEditor.class ) ) {
//			FlowEditor flowEditor = (FlowEditor) ( (ComponentNode) this.getParent() ).getEditor();
//			_frameListener = new PostFlowCheckerEditorFrameListener( this, editor, flowEditor );
//		}
//		else {
			_frameListener = new ComponentEditorFrameListener( this, editor );
//		}

		return _frameListener;
	}
	/**
	 * Remove all frame listeners for this node's component's editor.
	 */
	public void removeEditorFrameListeners() {
		if( _editor != null ) {
			_editor.removeEditorFrameListeners();
		}
	}


}
