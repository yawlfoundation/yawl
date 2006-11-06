/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors.net.renderer;

import javax.swing.JLabel;
import javax.swing.UIManager;

import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * This label displays the name of components in the flow editor.
 * 
 * @author Dean Mao
 * @author Daniel Gredler
 * @created Sep 13, 2004
 * @version $Revision: 1.2 $
 */
class NameLabel extends JLabel {

	private EditorDataProxy _proxy;
	private String _name;

	/**
	 * Default constructor.
	 */
	public NameLabel() {
		setVerticalAlignment( JLabel.CENTER );
		setHorizontalAlignment( JLabel.CENTER );
		setHorizontalTextPosition( JLabel.CENTER );
		setVerticalTextPosition( JLabel.BOTTOM );
		setFont( UIManager.getFont( "Tree.font" ) );
		setForeground( UIManager.getColor( "Tree.textForeground" ) );
		setBackground( UIManager.getColor( "Tree.textBackground" ) );
	}

	/**
	 * Sets the proxy whose domain object's name is to be displayed by this label.
	 * @param proxy The proxy whose domain object's name is to be displayed by this label.
	 * @throws CapselaException If there is an error retrieving the name from the domain object.
	 */
	public void setProxy( EditorDataProxy proxy ) throws EditorException {
		_proxy = proxy;
//		_name = "this should be the name, but it's not implemented yet?  ooo funny bone my funny bone!";
		_name = proxy.getLabel();
		this.setText( _name );
	}

}
