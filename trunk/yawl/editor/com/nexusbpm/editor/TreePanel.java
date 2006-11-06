/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

import com.nexusbpm.editor.tree.SharedNode;

/**
 * Base class for all panels containing trees.
 * 
 * @author     catch23
 * @author     Nathan Rose
 * @created    October 30, 2002
 */
public class TreePanel extends JPanel {
    private SharedNode _rootNode;

    private TreeModel _treeModel;

    private JTree _tree;

    /**
     * Default constructor.
     */
    public TreePanel() {
        // Empty.
    }
    
    /**
     * Constructor that calls initializeTree().
     * @see #initializeTree(JTree, boolean)
     */
    public TreePanel( JTree tree, boolean showRoot ) {
        initializeTree( tree, showRoot );
    }

    /**
     * Initializes the tree with the given root node. Creates a new tree model
     * with the root node 
     * @param rootNode the node to use as the root of the tree panel.
     * @throws CapselaException if there is an error populating the specified root
     *                          node.
     */
    protected void initializeTree( JTree tree, boolean showRoot ) {
        _tree = tree;
        _treeModel = tree.getModel();
        _rootNode = (SharedNode) _treeModel.getRoot();

        JScrollPane scrollPane = new JScrollPane( _tree );
        setLayout( new BorderLayout() );
        add( scrollPane, BorderLayout.CENTER );

        if( !_rootNode.isLeaf() ) {
            getTree().expandRow( 0 );
        }
        getTree().setRootVisible( showRoot );
    }

    /**
     * @return the root node of the tree panel.
     */
    public SharedNode getRoot() {
        return _rootNode;
    }

    /**
     * @return the tree used by the tree panel.
     */
    public JTree getTree() {
        return _tree;
    }

    /**
     * @return the tree model used by the tree panel.
     */
    public TreeModel getTreeModel() {
        return _treeModel;
    }

}
