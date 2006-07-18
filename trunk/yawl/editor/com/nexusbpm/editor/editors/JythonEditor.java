package com.nexusbpm.editor.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.util.syntax.JEditTextArea;
import com.nexusbpm.editor.util.syntax.PythonTokenMarker;
import com.nexusbpm.editor.util.syntax.SyntaxDocument;

/**
 * The editor for Jython components.
 * 
 * @see        com.ichg.capsela.domain.component.JythonComponent
 * @author     catch23
 * @author     Daniel Gredler
 * @created    October 28, 2002
 */
public class JythonEditor extends ComponentEditor {

	private JEditTextArea _codeEditor;
	private JEditorPane _resultEditor;
	private JEditorPane _errEditor;
    
	/**
	 * @see ComponentEditor#initializeUI()
	 */
	public JComponent initializeUI() throws EditorException {

		_codeEditor = new JEditTextArea();
		_codeEditor.setDocument( new SyntaxDocument() );
		_codeEditor.setText( data.getPlain( "code" ) );
		_codeEditor.setTokenMarker( new PythonTokenMarker() );

		_resultEditor = new JEditorPane();
		_resultEditor.setText( data.getPlain( "output" ) );

		_errEditor = new JEditorPane();
		_errEditor.setText( data.getPlain( "error" ) );

		addIsDirtyListener( _codeEditor );

		JScrollPane scrollResultPane = new JScrollPane( _resultEditor );
		JScrollPane scrollErrPane = new JScrollPane( _errEditor );

		JTabbedPane tabbedPanel = new JTabbedPane( JTabbedPane.TOP );
		tabbedPanel.addTab( "Code", _codeEditor );
		tabbedPanel.addTab( "Output", scrollResultPane );
		tabbedPanel.addTab( "Error", scrollErrPane );
		return tabbedPanel;
	}

	/**
	 * @see ComponentEditor#setUI(JComponent)
	 */
	protected void setUI( JComponent component ) {
		// Setup the UI.
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( component, BorderLayout.CENTER );
		this.setPreferredSize( new Dimension( 500, 300 ) );
		// Do stuff to give the code editor the focus.
		this.validate();
		_codeEditor.initialize();
		_codeEditor.setCaretPosition( 0 );
		_codeEditor.requestFocus();
	}

	/**
	 * @see ComponentEditor#saveAttributes()
	 */
	public void saveAttributes() {
        data.setPlain( "code", _codeEditor.getText() );
	}
}
