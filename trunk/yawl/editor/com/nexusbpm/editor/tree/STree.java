package com.nexusbpm.editor.tree;

import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.managed.DataProxy;

import com.nexusbpm.command.Command;
import com.nexusbpm.command.CopyNetCommand;
import com.nexusbpm.command.CopySpecificationCommand;
import com.nexusbpm.command.CreateFolderCommand;
import com.nexusbpm.command.CreateNetCommand;
import com.nexusbpm.command.CreateSpecificationCommand;
import com.nexusbpm.command.DeleteFileCommand;
import com.nexusbpm.command.RemoveDecompositionCommand;
import com.nexusbpm.command.RemoveNetCommand;
import com.nexusbpm.command.RemoveNetElementCommand;
import com.nexusbpm.command.RemoveNexusTaskCommand;
import com.nexusbpm.command.RemoveSpecificationCommand;
import com.nexusbpm.command.RemoveVirtualFolderCommand;
import com.nexusbpm.command.SaveSpecificationCommand;
import com.nexusbpm.editor.WorkflowEditor;
import com.nexusbpm.editor.persistence.EditorDataProxy;


/**
 * Capsela's JTree subclass used for all trees in Capsela.
 * 
 * A note on nodes and their editability: the tree is set to editable by default, but
 * TreeCellEditor's isCellEditable() method disallows editing until the "Rename" context
 * menu item is selected, at which point one edit is allowed, and then edits are disallowed
 * once again.
 * 
 * @author     catch23
 * @author     Daniel Gredler
 * @created    October 30, 2002
 */
public class STree extends JTree 
implements MouseListener, KeyListener, TreeSelectionListener, 
	DragGestureListener, DropTargetListener, DragSourceListener {

	private static final Log LOG = LogFactory.getLog( STree.class );

	private static DefaultTreeModel draggingTreeModel;

	/**
	 * The tree path to the currently selected node.
	 */
	private TreePath _selectedTreePath = null;

	/**
	 * The currently selected node.
	 */
	private SharedNode _selectedNode = null;

	/**
	 * Variables needed for Drag and Drop.
	 */
	private DragSource _dragSource = null;

	/**
	 * Constructor for a ComponentTree.
	 * 
	 * @param dtm the underlying tree data model.
	 * @param parentPanel the containing panel.
	 */
	public STree( SharedNodeTreeModel dtm ) {
		super( dtm );

		addTreeSelectionListener( this );

		_dragSource = new DragSource() {

			protected DragSourceContext createDragSourceContext( DragSourceContextPeer dscp, DragGestureEvent dgl, Cursor dragCursor, Image dragImage, Point imageOffset, Transferable t, DragSourceListener dsl ) {
				return new DragSourceContext( dscp, dgl, dragCursor, dragImage, imageOffset, t, dsl ) {

					protected void updateCurrentCursor( int dropOp, int targetAct, int status ) {

					}
				};
			}
		};

		DragGestureRecognizer dgr = _dragSource.createDefaultDragGestureRecognizer( this, DnDConstants.ACTION_COPY_OR_MOVE, this );
		new DropTarget( this, this );

		// Eliminates right mouse clicks as valid actions - useful especially
		// if you implement a JPopupMenu for the JTree
		dgr.setSourceActions( dgr.getSourceActions() & ~InputEvent.BUTTON3_MASK );

		SharedNodeRenderer renderer = new SharedNodeRenderer();
		setCellRenderer( renderer );
		SharedNodeEditor editor = new SharedNodeEditor( this, renderer );
		setCellEditor( editor );

		getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
		setShowsRootHandles( true );

		addMouseListener( this );
		addKeyListener( this );
		setEditable( true );
	}

	/**
	 *  Sets the draggingTreeModel attribute of the CapselaTree class
	 *
	 * @param  model  The new draggingTreeModel value
	 */
	public static void setDraggingTreeModel( DefaultTreeModel model ) {
		draggingTreeModel = model;
	}

	/**
	 *  Gets the draggingTreeModel attribute of the CapselaTree class
	 *
	 * @return    The draggingTreeModel value
	 */
	public static DefaultTreeModel getDraggingTreeModel() {
		return draggingTreeModel;
	}

	/**
	 * Gets the currently selected node.
	 * @return the currently selected node.
	 */
	public SharedNode getSelectedNode() {
		return _selectedNode;
	}


	/**
	 * Deletes the currently selected node.
	 */
	private void deleteCurrentNode() {
		LOG.debug( "ComponentTree.deleteCurrentNode" );
		SharedNode node = this.getSelectedNode();
		if( !node.isRoot() ) {
            Object data = node.getProxy().getData();
            if( data instanceof YSpecification ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new RemoveSpecificationCommand( node.getProxy() ) );
            }
            else if( data instanceof YNet ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new RemoveNetCommand( node.getProxy() ) );
            }
            else if( data instanceof YDecomposition ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new RemoveDecompositionCommand( node.getProxy() ) );
            }
            else if( data instanceof YExternalNetElement ) {
                if( data instanceof YAtomicTask ) {
                    WorkflowEditor.getExecutor().executeCommand(
                            new RemoveNexusTaskCommand( node.getProxy() ) );
                }
                else {
                    WorkflowEditor.getExecutor().executeCommand(
                            new RemoveNetElementCommand( node.getProxy() ) );
                }
            }
            else if( data instanceof File ) {
                int result = JOptionPane.showConfirmDialog(
                        WorkflowEditor.getInstance(),
                        "WARNING: This command is not undoable!\nDo you wish to proceed?",
                        "Delete File/Folder Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE );
                if( result == JOptionPane.YES_OPTION ) {
                    WorkflowEditor.getExecutor().executeCommand(
                            new DeleteFileCommand( node.getProxy() ) );
                }
            }
            else if( data instanceof String ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new RemoveVirtualFolderCommand( node ) );
            }
            else if( data != null ) {
                LOG.warn( "Cannot delete object - Unknown object type: " + data.getClass().getName() );
            }
            else {
                LOG.warn( "Cannot delete object - object is null!" );
            }
		}
		else {
			LOG.debug( "Cannot delete node - it is the root node" );
		}
	}

	/**
	 * Allows the user to rename the currently selected tree node.
	 */
	private void renameCurrentNode() {
		// Have to do this using invokeLater() because otherwise it doesn't work
		// using keyPressed() and F2 (see below)...
		Runnable r = new Runnable() {

			public void run() {
				TreePath path = STree.this.getSelectionPath();
				if( path != null ) {
					SharedNodeEditor.allowNextEdit();
					STree.this.setSelectionPath( path );
					STree.this.startEditingAtPath( path );
				}
			}
		};
		SwingUtilities.invokeLater( r );
	}
    
    private void openComponentEditor( SharedNode node ) {
        try {
            WorkflowEditor.getInstance().openComponentEditor( node.getProxy(), null );
        }
        catch( Exception e ) {
            LOG.error( "Error", e );
        }
    }
    
    private void openPropertiesEditor() {
        SharedNode node = this.getSelectedNode();
        try {
            WorkflowEditor.getInstance().openDataEditor( node.getProxy(), null );
        }
        catch( Exception e ) {
            LOG.error( "Error", e );
        }
    }

	// ###############################################################################################
	// ################################ DragGestureListener Interface ################################
	// ###############################################################################################

	/**
	 * DragGestureListener interface method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void dragGestureRecognized( DragGestureEvent e ) {
		LOG.debug( "start drag" );
		//Get the selected node
		SharedNode dragNode = getSelectedNode();
		if( _selectedTreePath != null && _selectedTreePath.getLastPathComponent() != null ) {
			SharedNode draggingNode = (SharedNode) _selectedTreePath.getLastPathComponent();
			DragAndDrop.setDraggingNode( draggingNode );
			//      ComponentNode draggingRootNode = (ComponentNode) draggingNode.getRoot();

			if( dragNode != null ) {

				//Get the Transferable Object
				Transferable transferable = dragNode.getProxy();
				//Select the appropriate cursor;
				//        Cursor cursor = null;

				STree.setDraggingTreeModel( (DefaultTreeModel) getModel() );
				//        if ((draggingRootNode.getTreeType() == ComponentNode.COMPONENTS_TREE) && (!Client.getPrincipal().isAdmin())) {
				//          LOG.debug("COPY OPERATION");
				//          cursor = DragSource.DefaultCopyDrop;
				//          ComponentTree.setDragCopyAction();
				//        } else {
				//          InputEvent ie = e.getTriggerEvent();
				//          if (ie.getModifiersEx() == (InputEvent.SHIFT_DOWN_MASK | InputEvent.BUTTON1_DOWN_MASK)) {
				//            LOG.debug("COPY OPERATION");
				//            cursor = DragSource.DefaultCopyDrop;
				//            ComponentTree.setDragCopyAction();
				//          } else {
				//            LOG.debug("MOVE OPERATION");
				//            cursor = DragSource.DefaultMoveDrop;
				//            ComponentTree.setDragMoveAction();
				//          }
				//        }

				//In fact the cursor is set to NoDrop because once an action is rejected
				// by a dropTarget, the dragSourceListener are no more invoked.
				// Setting the cursor to no drop by default is so more logical, because
				// when the drop is accepted by a component, then the cursor is changed by the
				// dropActionChanged of the default DragSource.

				//begin the drag
				_dragSource.startDrag( e, null, transferable, this );
			}
		}
	}

	// ###############################################################################################
	// ################################# DragSourceListener Interface ################################
	// ###############################################################################################

	/**
	 * DragSourceListener interface method
	 *
	 * @param  dsde  Description of the Parameter
	 */
	public void dragDropEnd( DragSourceDropEvent dsde ) {
		DragAndDrop.setDragSourceContext( dsde.getDragSourceContext() );
	}

	/**
	 * DragSourceListener interface method
	 *
	 * @param  dsde  Description of the Parameter
	 */
	public void dragEnter( DragSourceDragEvent dsde ) {
		DragAndDrop.setDragSourceContext( dsde.getDragSourceContext() );
	}

	/**
	 * DragSourceListener interface method
	 *
	 * @param  dsde  Description of the Parameter
	 */
	public void dragOver( DragSourceDragEvent dsde ) {
		DragAndDrop.setDragSourceContext( dsde.getDragSourceContext() );
	}

	/**
	 * DragSourceListener interface method
	 *
	 * @param  dsde  Description of the Parameter
	 */
	public void dropActionChanged( DragSourceDragEvent dsde ) {
		DragAndDrop.setDragSourceContext( dsde.getDragSourceContext() );
	}

	/**
	 * DragSourceListener interface method
	 *
	 * @param  dsde  Description of the Parameter
	 */
	public void dragExit( DragSourceEvent dsde ) {
		DragAndDrop.setDragSourceContext( dsde.getDragSourceContext() );
	}

	// ###############################################################################################
	// ################################# DropTargetListener Interface ################################
	// ###############################################################################################

	/**
	 * DropTargetListener interface method - What we do when drag is released
	 *
	 * @param  e  Description of the Parameter
	 */
	public void drop( DropTargetDropEvent e ) {
		LOG.debug( "doing drop" );
		try {
			Transferable tr = e.getTransferable();

			//flavor not supported, reject drop
			if( !tr.isDataFlavorSupported( EditorDataProxy.PROXY_FLAVOR ) ) {
				LOG.debug( "Transferable flavor unsupported" );
				e.rejectDrop();
                return;
			}

			//get new parent node
			Point loc = e.getLocation();
			TreePath destinationPath = getPathForLocation( loc.x, loc.y );
            if( destinationPath == null )
                return;
			SharedNode draggingNode = DragAndDrop.getDraggingNode();
            SharedNode destinationNode = (SharedNode) destinationPath.getLastPathComponent();

			if( !isDropValid( draggingNode, destinationPath ) ) {
				LOG.debug( "testDropTarget failed" );
				e.rejectDrop();
				return;
			}

//			SharedNode destinationNode = (SharedNode) destinationPath.getLastPathComponent();
//			SharedNode oldParent = (SharedNode) draggingNode.getParent();

//			if( isDropCopy( draggingNode, destinationPath ) ) {
				LOG.debug( "performing COPY action" );
                WorkflowEditor.getExecutor().executeCommand(
                        createCopyCommand( draggingNode, destinationNode ) );
//				EditorCommand.executeCopyCommand( draggingNode, destinationNode);
				e.acceptDrop( DnDConstants.ACTION_COPY );
//			}
//			else {
//				LOG.debug( "performing MOVE action" );
//				// ClientOperation.executeMoveCommand( draggingNode.getIndependentController(), oldParent.getIndependentController(), destinationNode.getIndependentController(), null, null );
//				e.acceptDrop( DnDConstants.ACTION_MOVE );
//			}

			e.getDropTargetContext().dropComplete( true );

//			TreePath parentPath = new TreePath( destinationNode.getPath() );
//			expandPath( parentPath );
		}
		catch( Exception ex ) {
			LOG.error("Exception trying to copy an element!", ex );
			ex.printStackTrace();
		}
	}
    
    private Command createCopyCommand( SharedNode source, SharedNode target ) {
        Command cmd;
        Object sourceObject = source.getProxy().getData();
        
        if( sourceObject instanceof YSpecification ) {
            cmd = new CopySpecificationCommand(
                    source.getProxy(),
                    target.getProxy(),
                    (SharedNodeTreeModel) getModel() );
        }
        else if( sourceObject instanceof YNet ) {
            cmd = new CopyNetCommand(
                    source.getProxy(),
                    target.getProxy(),
                    (SharedNodeTreeModel) getModel() );
        }
        else {
            throw new RuntimeException( "Copy attempt unsupported: " + sourceObject.getClass() );
        }
        
        return cmd;
    }

	/**
	 * DropTargetListener interface method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void dragEnter( DropTargetDragEvent e ) {
	}

	/**
	 * DropTargetListener interface method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void dragExit( DropTargetEvent e ) {
		DragAndDrop.setMouseCursorToRejectDrop();
	}

	/**
	 * DropTargetListener interface method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void dragOver( DropTargetDragEvent e ) {
		//set cursor location. Needed in setCursor method
		Point cursorLocationBis = e.getLocation();
		TreePath destinationPath = getPathForLocation( cursorLocationBis.x, cursorLocationBis.y );
		SharedNode draggingNode = DragAndDrop.getDraggingNode();
		// if destination path is okay accept drop...
		if( isDropValid( draggingNode, destinationPath ) ) {
//			boolean isCopy = isDropCopy( draggingNode, destinationPath );
//			if( isCopy ) {
				e.acceptDrag( DnDConstants.ACTION_COPY );
				DragAndDrop.setMouseCursorToAcceptDropForCopy();
//			}
//			else {
//				e.acceptDrag( DnDConstants.ACTION_MOVE );
//				DragAndDrop.setMouseCursorToAcceptDropForMove();
//			}
		}
		// ...otherwise reject drop
		else {
			e.rejectDrag();
			DragAndDrop.setMouseCursorToRejectDrop();
		}
	}

	/**
	 * DropTaregetListener interface method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void dropActionChanged( DropTargetDragEvent e ) {
	}

	/**
	 * Convenience method to test whether drop location is valid
	 *
	 * @param  draggingNode  The destination path
	 * @param  destinationPath      The path for the node to be dropped
	 * @return boolean             true if no problems
	 */
	private boolean isDropValid( SharedNode draggingNode, TreePath destinationPath ) {
//		RuntimeException e = new RuntimeException("This method needs to be reimplemented for YAWL context");
//		LOG.info("shared node=" + draggingNode.getProxy().getData().toString());
//		throw e;
        if( destinationPath == null || draggingNode == null ) {
            // Invalid drop location.
            return false;
        }
		SharedNode destinationNode = (SharedNode) destinationPath.getLastPathComponent();
        if( destinationNode.equals( draggingNode ) ) {
            // Destination cannot be same as source.
            return false;
        }
        assert draggingNode.getProxy() != null : "draggingNode.getProxy() was null";
        assert draggingNode.getProxy().getContext() != null :
            "draggingNode's proxy is not connected to a context";
        assert destinationNode.getProxy() != null : "destinationNode.getProxy() was null";
        assert destinationNode.getProxy().getContext() != null :
            "destinationNode's proxy is not connected to a context";
//        System.out.println( draggingNode.getProxy().getData().getClass().toString() );
//        System.out.println( destinationNode.getProxy().getData().getClass().toString() );
        if( draggingNode.getProxy().getData() instanceof YSpecification ) {
            if( destinationNode.getProxy().getData() instanceof YSpecification ||
                    destinationNode.getProxy().getData() instanceof YDecomposition ||
                    destinationNode.getProxy().getData() instanceof YExternalNetElement ) {
                // cannot drag a spec into anything but a folder
                return false;
            }
        }
        else if( draggingNode.getProxy().getData() instanceof YDecomposition ) {
            if( ! ( destinationNode.getProxy().getData() instanceof YSpecification ) ) {
                // can only drag a decomposition into a specification
                return false;
            }
        }
        else if( draggingNode.getProxy().getData() instanceof YExternalNetElement ) {
            // net elements must be dragged into the graph editor, not the tree
            return false;
        }
        else {
            // if the dragging node is not a spec, decomp, or task, then we don't accept it
            return false;
        }
//        if( !destinationNode.getController().isFolder() ) {
//            // This node does not allow children.
//            return false;
//        }
//		if( destinationNode.getController().isFlow() ) {
//			// Cannot drop directly into a flow, need to use the flow editor.
//			return false;
//		}
//		if( draggingNode != null && draggingNode.getParent().equals( destinationNode ) ) {
//			// Destination node cannot be a parent.
//			return false;
//		}
//		if( destinationNode.isInComponentsFolder() && !Client.getPrincipal().isAdmin() ) {
//			// Destination node cannot be from the Components list (unless the user is admin).
//			return false;
//		}
		return true;
	}

	// ###############################################################################################
	// ############################### TreeSelectionListener Interface ###############################
	// ###############################################################################################

	/**
	 * TreeSelectionListener - sets selected node
	 *
	 * @param  evt  Description of the Parameter
	 */
	public void valueChanged( TreeSelectionEvent evt ) {
		LOG.debug( "Setting _selectedTreePath" );
		_selectedTreePath = evt.getNewLeadSelectionPath();
		if( _selectedTreePath == null ) {
			_selectedNode = null;
			return;
		}
		_selectedNode = (SharedNode) _selectedTreePath.getLastPathComponent();
	}

	// ###############################################################################################
	// ################################### MouseListener Interface ###################################
	// ###############################################################################################

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseClicked( MouseEvent e ) {
		STree tree = (STree) e.getSource();
		Point pt = e.getPoint();
		int x = (int) pt.getX();
		int y = (int) pt.getY();
		TreePath path = tree.getPathForLocation( x, y );
		if( path == null ) {
			// When the user clicks off of a tree node, make sure the node gets deselected.
			tree.setSelectionPath( null );
		}
		else if( e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1 ) {
			SharedNode node = (SharedNode) path.getLastPathComponent();
			if( node.isLeaf() ) {
				// When the user double clicks on a tree leaf node, open up the editor.
				// Don't do it if the node is not a leaf, because double-clicking on a
				// non-leaf node just expands and contracts said node.
                STree.this.openComponentEditor( node );
			}
		}
	}

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseEntered( MouseEvent e ) {

	}

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseExited( MouseEvent e ) {

	}

	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseReleased( MouseEvent e ) {

	}

	/**
	 * @see MouseListener#mousePressed(MouseEvent)
	 */
	public void mousePressed( MouseEvent e ) {
		if( e.getModifiersEx() == ( MouseEvent.BUTTON3_DOWN_MASK ) &&
                ! ((SharedNodeTreeModel)getModel()).isReadOnly() ) {
			JPopupMenu menu = createPopupMenu( e.getPoint(), e.getSource() );
			if( menu != null ) {
				menu.show( this, e.getX(), e.getY() );
			}
		}
	}

	/**
	 * Creates the popup menu corresponding to the specified point and component tree.
	 * May return <code>null</code> if no popup menu applies.
	 */
	private JPopupMenu createPopupMenu( final Point pt, final Object comp ) {

		final STree tree = (STree) comp;
		TreePath selPath = tree.getPathForLocation( (int) pt.getX(), (int) pt.getY() );
		if( selPath == null ) { return null; }
        
		JPopupMenu menu = new JPopupMenu();
        menu.setLightWeightPopupEnabled( false );
		tree.setSelectionPath( selPath );
		final SharedNode node = (SharedNode) selPath.getLastPathComponent();
        
        JMenuItem createFolder = new JMenuItem( new AbstractAction( "Create Folder" ) {
            public void actionPerformed( ActionEvent e ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new CreateFolderCommand( node.getProxy(), "New Folder",
                                (SharedNodeTreeModel) node.getTreeModel() ) );
            }
        });
        
        JMenuItem createSpecification = new JMenuItem( new AbstractAction( "Create Specification" ) {
            public void actionPerformed( ActionEvent e ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new CreateSpecificationCommand( node.getProxy(), "New Specification",
                                (SharedNodeTreeModel) getModel() ) );
            }
        });
        
        JMenuItem createNet = new JMenuItem();
        createNet.setAction( new AbstractAction( "Create Net" ) {
            public void actionPerformed( ActionEvent e ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new CreateNetCommand( node.getProxy(), "New Net", (SharedNodeTreeModel) getModel() ) );
            }
        });
        
        JMenuItem save = new JMenuItem( new AbstractAction( "Save Specification" ) {
            public void actionPerformed( ActionEvent e ) {
                WorkflowEditor.getExecutor().executeCommand(
                        new SaveSpecificationCommand( node.getProxy() ) );
            }
        });
        
        JMenuItem edit = new JMenuItem( new AbstractAction( "Edit" ) {
            public void actionPerformed( ActionEvent e ) {
                STree.this.openComponentEditor( STree.this.getSelectedNode() );
            }
        });
        
        JMenuItem rename = new JMenuItem( new AbstractAction( "Rename" ) {
            public void actionPerformed( ActionEvent e ) {
                STree.this.renameCurrentNode();
            }
        });
        JMenuItem properties = new JMenuItem( new AbstractAction( "Properties" ) {
            public void actionPerformed( ActionEvent e ) {
                STree.this.openPropertiesEditor();
            }
        });
        
        JMenuItem delete = new JMenuItem( new AbstractAction( "Delete" ) {
            public void actionPerformed( ActionEvent e ) {
                STree.this.deleteCurrentNode();
            }
        });
        
        Object data = node.getProxy().getData();
        assert data != null : "Proxy's data was null!";
        
        if( data instanceof DatasourceRoot ) {
            menu.add( createFolder );
            menu.add( createSpecification );
        } else if( data instanceof DatasourceFolder ) {
            menu.add( createFolder );
            menu.add( createSpecification );
            menu.add( new JPopupMenu.Separator() );
            menu.add( rename );
            menu.add( new JPopupMenu.Separator() );
            menu.add( delete );
        } else if( data instanceof YSpecification ) {
            menu.add( createNet );
            menu.add( new JPopupMenu.Separator() );
            menu.add( save );
            menu.add( new JPopupMenu.Separator() );
            menu.add( rename );
            menu.add( new JPopupMenu.Separator() );
            menu.add( delete );
        } else if( data instanceof YDecomposition ) {
            if( data instanceof YNet ) {
                menu.add( edit );
                menu.add( new JPopupMenu.Separator() );
                menu.add( rename );
                menu.add( properties );
                menu.add( new JPopupMenu.Separator() );
                menu.add( delete );
            }
            else {
                menu.add( delete );
            }
            
        } else if( data instanceof YExternalNetElement ) {
            if( data instanceof YAtomicTask ) {
                menu.add( edit );
                menu.add( new JPopupMenu.Separator() );
                menu.add( rename );
                menu.add( properties );
                menu.add( new JPopupMenu.Separator() );
                menu.add( delete );
            }
            else {
                menu.add( delete );
            }
        } else {
            menu = null;
        }
        
        if( menu != null ) {
            menu.add( new AbstractAction( "DEBUG LOG" ) {
                public void actionPerformed( ActionEvent e ) {
                    SharedNode anode = node;
                    DataProxy proxy = null;
                    Object data = null;
                    Class dataClass = null;
                    try {
                        if( node != null ) {
                            proxy = node.getProxy();
                            if( proxy != null ) {
                                data = proxy.getData();
                                if( data != null ) {
                                    dataClass = data.getClass();
                                }
                            }
                        }
                    }
                    catch( Throwable t ) {
                        LOG.error( "Error", t );
                    }
                    LOG.warn(
                            "node:" + node +
                            "\nproxy:" + proxy +
                            "\ndata:" + data +
                            "\ndataClass:" + dataClass +
                            "\ndataID:" + getID( data ),
                            new Exception("Stacktrace...").fillInStackTrace() );
                }
                private String getID( Object o ) {
                    if( o instanceof YSpecification ) {
                        return ((YSpecification) o).getID();
                    }
                    else if( o instanceof YDecomposition ) {
                        return ((YDecomposition) o).getId();
                    }
                    else if( o instanceof YExternalNetElement ) {
                        return ((YExternalNetElement) o).getID();
                    }
                    else return "";
                }
            });
        }
        
		return menu;
	}
    
	// ###############################################################################################
	// #################################### KeyListener Interface ####################################
	// ###############################################################################################

	/**
	 * Empty implementation.
	 * @see KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased( KeyEvent e ) {
		// Empty.
	}

	/**
	 * Empty implementation.
	 * @see KeyListener#keyTyped(java.awt.event.KeyEvent)
	 */
	public void keyTyped( KeyEvent e ) {
		// Empty.
	}

	/**
	 * Handles keypresses on a component tree for opening, renaming, and deleting
	 * components.
	 * @see KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed( KeyEvent e ) {
		if( e.getKeyCode() == KeyEvent.VK_DELETE ) {
			// Delete key pressed; delete the selected node.
			this.deleteCurrentNode();
		}
		else if( e.getKeyCode() == KeyEvent.VK_F2 ) {
			// F2 key pressed (standard Windows rename key); rename the selected node.
			this.renameCurrentNode();
		}
	}
}
