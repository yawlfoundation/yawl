package com.nexusbpm.editor.editors.net.renderer;

import javax.swing.JEditorPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.persistence.DataProxy;

/**
 * This text area displays the description of components in the flow editor.
 * 
 * @author Dean Mao
 * @author Daniel Gredler
 * @created Sep 13, 2004
 * @version $Revision: 1.13 $
 */
public class DescriptionTextArea extends JEditorPane {

	private final static Log LOG = LogFactory.getLog( DescriptionTextArea.class );

	private DataProxy _proxy;
	private String _description;

	/**
	 * @see JEditorPane#JEditorPane(java.lang.String, java.lang.String)
	 */
	public DescriptionTextArea( String type, String text ) {
		super( type, text );
	}

	/**
	 * Sets the proxy whose domain object's description is to be displayed by this text area.
	 * @param proxy The proxy whose domain object's description is to be displayed by this text area.
	 * @throws CapselaException If there is an error retrieving the description from the domain object.
	 */
	public void setProxy( DataProxy proxy ) throws EditorException {
		_proxy = proxy;
		_description = "This is the fancy nancy description.  Please replace me with something less fancy.";
		this.setText( _description );
	}

	/**
	 * @see JEditorPane#setText(java.lang.String)
	 */
	public void setText( String text ) {
		if( text != null && text.length() > 0 ) {
			text = text.replaceAll( "\n", "<br>" );
			super.setText( "<center><font color=\"#337733\" face=Arial size=-1>" + text + "</font></center>" );
		}
		else {
			super.setText( "" );
		}
	}

}
