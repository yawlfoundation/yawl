package com.nexusbpm.editor.tree;

import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;

/**
 * Client drag and drop support. This is changing, but getting better...
 * 
 * @author Daniel Gredler
 * @author Dean Mao
 */
public class DragAndDrop {

	/**
	 * The drag source context, used to modify the mouse cursor.
	 */
	private static DragSourceContext _dragSourceContext;
	/**
	 * Sets the current drag source context.
	 * @param dragSourceContext the drag source context to set.
	 */
	public static void setDragSourceContext(DragSourceContext dragSourceContext) {
		_dragSourceContext = dragSourceContext;
	}//setDragSourceContext()
	/**
	 * Sets the mouse cursor to the "move" cursor.
	 */
	public static void setMouseCursorToAcceptDropForMove() {
		if( _dragSourceContext != null ) {
			_dragSourceContext.setCursor( DragSource.DefaultMoveDrop );
		}//if
	}//setMouseCursorToAcceptDropForMove()
	/**
	 * Sets the mouse cursor to the "copy" cursor.
	 */
	public static void setMouseCursorToAcceptDropForCopy() {
		if( _dragSourceContext != null ) {
			_dragSourceContext.setCursor( DragSource.DefaultCopyDrop );
		}//if
	}//setMouseCursorToAcceptDropForCopy()
	/**
	 * Sets the mouse cursor to the "reject" cursor.
	 */
	public static void setMouseCursorToRejectDrop() {
		if( _dragSourceContext != null ) {
			_dragSourceContext.setCursor( DragSource.DefaultMoveNoDrop );
		}//if
	}//setMouseCursorToRejectDrop()


	/**
	 * The component node that the user is currently dragging around.
	 */
	private static SharedNode _draggingNode;
	/**
	 * Sets the node that the user is currently dragging around.
	 * @param draggingNode the node that the user is dragging around.
	 */
	public static void setDraggingNode(SharedNode draggingNode) {
		_draggingNode = draggingNode;
	}//setDraggingNode()
	/**
	 * Gets the node that the user is currently dragging around.
	 * @return the node that the user is currently dragging around.
	 */
	public static SharedNode getDraggingNode() {
		return _draggingNode;
	}//getDraggingNode()
}