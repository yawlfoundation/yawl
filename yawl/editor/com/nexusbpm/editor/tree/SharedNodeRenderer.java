package com.nexusbpm.editor.tree;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


/**
 *  Description of the Class
 *
 * @author     Dean Mao
 * @created    December 3, 2002
 */
public class SharedNodeRenderer extends DefaultTreeCellRenderer {

  /**
   *  Gets the treeCellRendererComponent attribute of the CapselaTreeNodeRenderer object
   *
   * @param  tree      Description of the Parameter
   * @param  value     Description of the Parameter
   * @param  sel       Description of the Parameter
   * @param  expanded  Description of the Parameter
   * @param  leaf      Description of the Parameter
   * @param  row       Description of the Parameter
   * @param  hasFocus  Description of the Parameter
   * @return           The treeCellRendererComponent value
   * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
   */
  public Component getTreeCellRendererComponent( JTree tree, Object value,
		boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus ) {
		super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row,
			hasFocus );
		SharedNode node = (SharedNode) value;
		setIcon( node.getProxy().iconSmall() );
		String candidate = node.getProxy().getLabel();
		if (candidate.endsWith("/")) candidate = candidate.substring(0, candidate.length() - 1 );
		setText( candidate );
		return this;
	}
  
}

