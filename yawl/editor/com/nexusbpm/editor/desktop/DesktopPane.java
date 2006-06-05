package com.nexusbpm.editor.desktop;

import java.awt.Point;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JDesktopPane;
import javax.swing.tree.TreeNode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import samples.bboard.Client;

import com.nexusbpm.editor.tree.DragAndDrop;

/**
 * The Capsela desktop, where all component editors are displayed.
 * 
 * @author catch23
 */
public class DesktopPane extends JDesktopPane implements DropTargetListener {

  private static final Log LOG = LogFactory.getLog( DesktopPane.class );

  /**
   * Default constructor.
   */
  public DesktopPane() {
    super();
    new DropTarget(this, this);
  }

  /**
   * @see DropTargetListener#dropActionChanged(DropTargetDragEvent)
   */
  public void dropActionChanged(DropTargetDragEvent event) {
  }

  /**
   * @see DropTargetListener#dragEnter(DropTargetDragEvent)
   */
  public void dragEnter(DropTargetDragEvent event) {
  }

  /**
   * @see DropTargetListener#dragExit(DropTargetEvent)
   */
  public void dragExit(DropTargetEvent event) {
  	DragAndDrop.setMouseCursorToRejectDrop();
  }

  /**
   * @see DropTargetListener#dragOver(DropTargetDragEvent)
   */
  public void dragOver(DropTargetDragEvent event) {
  	TreeNode draggingNode = DragAndDrop.getDraggingNode();
  	if( isDropAcceptable(draggingNode) ) {
      event.acceptDrag(DnDConstants.ACTION_MOVE);
  	  DragAndDrop.setMouseCursorToAcceptDropForMove();
  	} else {
  	  event.rejectDrag();
  	  DragAndDrop.setMouseCursorToRejectDrop();
  	}
  }

  /**
   * Returns <code>true</code> if the specified <code>ComponentNode</code> may
   * be dropped on the desktop.
   */
  private boolean isDropAcceptable(TreeNode draggingNode) {
	  throw new RuntimeException("this needs a new context for YAWL");
  }

  /**
   * Method to handle drop events. Dropping a component node on the desktop
   * opens the editor for the node's component.
   * 
   * @see DropTargetListener#drop(DropTargetDropEvent)
   */
  public void drop(DropTargetDropEvent event) {
  	TreeNode draggingNode = DragAndDrop.getDraggingNode();
    if( isDropAcceptable(draggingNode) ) {
      Point location = event.getLocation();
//        CapselaInternalFrame internalFrame = draggingNode.getFrame();
//        if (Client.numWindowMenuItems() == 0) {
//          Client.openInternalFrame(internalFrame, new Point(0,0), true);
//        } else {
//          Client.openInternalFrame(internalFrame, location, false);
//        }
      throw new RuntimeException("This needs a new context for YAWL.  originally this would open up the editor on the desktop");
    }
  }

}
