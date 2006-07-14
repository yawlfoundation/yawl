package com.nexusbpm.editor.component;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import au.edu.qut.yawl.elements.YAtomicTask;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.data.YVariable;

import com.nexusbpm.editor.desktop.ComponentEditor;
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

		YAtomicTask jython = (YAtomicTask) _proxy.getData();
		YNet network = ( jython.getParent());

		_codeEditor = new JEditTextArea();
		_codeEditor.setDocument( new SyntaxDocument() );
		_codeEditor.setText( ((YVariable) network.getLocalVariable(jython.getID() + "." + "code")).getInitialValue() );
		_codeEditor.setTokenMarker( new PythonTokenMarker() );

		_resultEditor = new JEditorPane();
		_resultEditor.setText( ((YVariable) network.getLocalVariable(jython.getID() + "." + "output")).getInitialValue() );

		_errEditor = new JEditorPane();
		_errEditor.setText( ((YVariable) network.getLocalVariable(jython.getID() + "." + "error")).getInitialValue() );

		addIsDirtyListener( _codeEditor );
		addIsDirtyListener( _resultEditor );
		addIsDirtyListener( _errEditor );

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
		JythonComponent jython = (JythonComponent) _proxy.getData();
		jython.setCode( _codeEditor.getText() );
	}

}
