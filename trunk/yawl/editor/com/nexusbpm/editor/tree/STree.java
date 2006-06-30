package com.nexusbpm.editor.tree;

import java.awt.Component;
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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.command.EditorCommand;
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
	 * The containing panel.
	 */
	private Component _parentPanel;

	/**
	 * Constructor for a ComponentTree.
	 * 
	 * @param dtm the underlying tree data model.
	 * @param parentPanel the containing panel.
	 */
	public STree( TreeModel dtm, java.awt.Component parentPanel ) {
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

		_parentPanel = parentPanel;
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
	 * Deletes the currently selected node, as long as the corresponding component
	 * is not locked by someone else.
	 */
	private void deleteCurrentNode() {
		LOG.debug( "ComponentTree.deleteCurrentNode" );
		SharedNode node = this.getSelectedNode();
		if( !node.isRoot() ) {
			throw new RuntimeException("please implement the delete node operation!");
		}
		else {
			LOG.debug( "Cannot delete node - is not root" );
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
		LOG.error( "doing drop" );
		try {
			Transferable tr = e.getTransferable();

			//flavor not supported, reject drop
			if( !tr.isDataFlavorSupported( EditorDataProxy.PROXY_FLAVOR ) ) {
				LOG.debug( "Transferable flavor unsupported" );
				e.rejectDrop();
			}

			//get new parent node
			Point loc = e.getLocation();
			TreePath destinationPath = getPathForLocation( loc.x, loc.y );
			SharedNode draggingNode = DragAndDrop.getDraggingNode();

			if( !isDropValid( draggingNode, destinationPath ) ) {
				LOG.debug( "testDropTarget failed" );
				e.rejectDrop();
				return;
			}

			SharedNode destinationNode = (SharedNode) destinationPath.getLastPathComponent();
//			SharedNode oldParent = (SharedNode) draggingNode.getParent();

			if( isDropCopy( draggingNode, destinationPath ) ) {
				LOG.debug( "performing COPY action" );
				EditorCommand.executeCopyCommand( draggingNode, destinationNode);
				e.acceptDrop( DnDConstants.ACTION_COPY );
			}
			else {
				LOG.debug( "performing MOVE action" );
				// ClientOperation.executeMoveCommand( draggingNode.getIndependentController(), oldParent.getIndependentController(), destinationNode.getIndependentController(), null, null );
				e.acceptDrop( DnDConstants.ACTION_MOVE );
			}

			e.getDropTargetContext().dropComplete( true );

			TreePath parentPath = new TreePath( destinationNode.getPath() );
			expandPath( parentPath );
		}
		catch( Exception ex ) {
			LOG.error("Exception trying to copy specification.", ex );
			ex.printStackTrace();
		}

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
			boolean isCopy = isDropCopy( draggingNode, destinationPath );
			if( isCopy ) {
				e.acceptDrag( DnDConstants.ACTION_COPY );
				DragAndDrop.setMouseCursorToAcceptDropForCopy();
			}
			else {
				e.acceptDrag( DnDConstants.ACTION_MOVE );
				DragAndDrop.setMouseCursorToAcceptDropForMove();
			}
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
		return true;
//		throw e;
		//		if( destinationPath == null ) {
//			// Invalid drop location.
//			return false;
//		}
//		SharedNode destinationNode = (SharedNode) destinationPath.getLastPathComponent();
//		if( !destinationNode.getController().isFolder() ) {
//			// This node does not allow children.
//			return false;
//		}
//		if( destinationNode.getController().isFlow() ) {
//			// Cannot drop directly into a flow, need to use the flow editor.
//			return false;
//		}
//		if( destinationNode.equals( draggingNode ) ) {
//			// Destination cannot be same as source.
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
//		return true;
	}

	private boolean isDropCopy( SharedNode draggingNode, TreePath destinationPath ) {
		// TODO: as per Zubin (May 10 2005), everything is a copy operation until further notice.
		return true;
		//    // It's a copy instead of a move if the user is dragging something into the network,
		//    // dragging something out of the network, or dragging something out of the components tree.
		//    ComponentNode destinationNode = (ComponentNode) destinationPath.getLastPathComponent();
		//    boolean fromNetworkToX_or_fromXToNetwork = ( destinationNode.isInNetworkFolder() != draggingNode.isInNetworkFolder() );
		//    boolean fromComponentsToX = draggingNode.isInComponentsFolder();
		//    return fromNetworkToX_or_fromXToNetwork || fromComponentsToX;
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
//				try {
//					CapselaInternalFrame internalFrame = node.getFrame();
//					Client.openInternalFrame( internalFrame, new Point( 0, 0 ), true );
//				}
//				catch( CapselaException ex ) {
//					LOG.error( ex );
//				}
				new RuntimeException("This needs to be reimplemented for YAWL context").printStackTrace();
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
		if( e.getModifiersEx() == ( MouseEvent.BUTTON3_DOWN_MASK ) ) {
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
		tree.setSelectionPath( selPath );
		final SharedNode node = (SharedNode) selPath.getLastPathComponent();

		if( ! node.isRoot() ) {
			menu.add( new AbstractAction( "Edit" ) {
				public void actionPerformed( ActionEvent e ) {
//					ComponentTree.this.openCurrentNode();
					throw new RuntimeException("This needs to be reimplemented in the YAWL context");
					
				}
			} );

			Action executeAction = new AbstractAction( "Execute" ) {
				public void actionPerformed( ActionEvent e ) {
					EditorDataProxy proxy = node.getProxy();
					//ClientOperation.createInstanceAndRun( ctrl );
					throw new RuntimeException("This needs to be reimplemented in the YAWL context");
				}
			};
			// Disable the "Execute" menu item if it is for a flow that has errors in it. Note
			// that we do not force a load of the object here. If we have a PersistentDomainObject
			// in the cache, we will know enough to disable it if necessary; if we just have
			// a VirtualDomainObject, we do not care enough to take the time to load the object
			// from the database. This method is called every time the user right clicks on a
			// component tree, and it needs to be very fast.

			// TODO the following block is commented because it could cause lazy initialization depth error -- see Dean about it

			//      if(node.getController().isFlow()) {
			//        try {
			//          DomainObject obj = node.getController().getDomainObject();
			//          if( obj instanceof FlowComponent ) {
			//            FlowComponent flow = (FlowComponent) obj;
			//            if( Helper.isInitialized(flow.getComponents()) ) {
			//              FlowUnderstanding fu = new FlowUnderstanding(flow);
			//              executeAction.setEnabled( ! fu.containsErrors() );
			//            }
			//          }
			//        } catch (CapselaException e) {
			//          LOG.error(e);
			//        }
			//      }
			menu.add( executeAction );

			menu.add( new JSeparator() );

			Action renameAction = new AbstractAction( "Rename" ) {
				public void actionPerformed( ActionEvent e ) {
					STree.this.renameCurrentNode();
				}
			};
			menu.add( renameAction );

			Action deleteAction = new AbstractAction( "Delete" ) {
				public void actionPerformed( ActionEvent e ) {
					STree.this.deleteCurrentNode();
				}
			};
			menu.add( deleteAction );

		}

		if( menu.getSubElements().length > 0 ) {
			menu.add( new JSeparator() );
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
