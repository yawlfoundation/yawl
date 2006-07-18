package com.nexusbpm.editor.editors;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.FileChooser;
import com.nexusbpm.editor.exception.EditorException;

/**
 * Editor for the email sender component.
 * 
 * @see        com.ichg.capsela.domain.component.EmailSenderComponent
 * @author     catch23
 * @author     Daniel Gredler
 * @created    October 28, 2002
 */
public class EmailSenderEditor extends ComponentEditor {

	private static final Log LOG = LogFactory.getLog( EmailSenderEditor.class );

	private JEditorPane _editor;
	public JTextField _toAddressField;
	private JTextField _fromAddressField;
	private JTextField _subjectField;
	private JButton _fileBrowseButton;
	private JTextField _fileAttachmentField;
//	private YTask _sender;

	/**
	 * @see ComponentEditor#initializeUI()
	 */
	public JComponent initializeUI() throws EditorException {

		String to_addresses = data.getPlain( "toAddress" );
		if( to_addresses == null ) to_addresses = "";
		_toAddressField = new JTextField( "", 30 );
		_toAddressField.setText( to_addresses );

		String from_address = "";//data.getPlain();
		if( from_address == null ) from_address = "capsela@ichotelsgroup.com";
		_fromAddressField = new JTextField( "", 30 );
		_fromAddressField.setText( from_address );

		String subject = data.getPlain( "subject" );
		if( subject == null ) subject = "";
		_subjectField = new JTextField( "", 30 );
		_subjectField.setText( subject );

		String body = data.getPlain( "body" );
		if( body == null ) body = "";
		_editor = new JEditorPane();
		_editor.setText( body );

//		FileAttribute attachment = _sender.getFileAttachment();
//		String attachmentFileName = ( attachment != null ? attachment.getDisplayedName() : "" );
		_fileAttachmentField = new JTextField();
//		_fileAttachmentField.setText( attachmentFileName );

		_fileBrowseButton = new JButton( "Browse" );

		addIsDirtyListener( _editor );
		addIsDirtyListener( _toAddressField );
		addIsDirtyListener( _fromAddressField );
		addIsDirtyListener( _subjectField );
		addIsDirtyListener( _fileBrowseButton );
		addIsDirtyListener( _fileAttachmentField );

		JPanel addressPanel = new JPanel();
		addressPanel.setLayout( new GridLayout( 3, 2 ) );
		addressPanel.add( new JLabel( "To Address: " ) );
		addressPanel.add( _toAddressField );

		addressPanel.add( new JLabel( "From Address: " ) );
		addressPanel.add( _fromAddressField );

		addressPanel.add( new JLabel( "Subject: " ) );
		addressPanel.add( _subjectField );

		_fileBrowseButton.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				JFileChooser fc = FileChooser.getStandardChooser();
				int returnVal = fc.showOpenDialog( EmailSenderEditor.this );
				if( returnVal == JFileChooser.APPROVE_OPTION ) {
					File file = fc.getSelectedFile();
					_fileAttachmentField.setText( file.getPath() );
					String path = file.getPath();
					try {
//						FileAttribute fa = new FileAttribute( (Component) _controller.getPersistentDomainObject(), "", file.getName(), true );
//						String localDir = path.substring( 0, path.lastIndexOf( System.getProperty( "file.separator" ) ) );
//						ClientHelper.makeDisplayedFileRemote( fa, localDir );
//						_sender.setFileAttachment( file );
						_fileAttachmentField.setText( file.getName() );
					}
					catch( Exception ce ) {
						// CapselaException is already logged.
					}
				}
			}
		} );

		JPanel attachmentPanel = new JPanel();
		attachmentPanel.setLayout( new GridLayout( 1, 2 ) );
		attachmentPanel.add( _fileBrowseButton );
		attachmentPanel.add( _fileAttachmentField );

		JPanel ui = new JPanel();
		ui.setBorder(new EmptyBorder(10,10,10,10));
		BorderLayout layout = new BorderLayout();
		ui.setLayout( layout );
		ui.add( new JScrollPane( _editor ), BorderLayout.CENTER );
		ui.add( attachmentPanel, BorderLayout.SOUTH );
		ui.add( addressPanel, BorderLayout.NORTH );
		return ui;
	}

	/**
	 * @see ComponentEditor#setUI(JComponent)
	 */
	protected void setUI( JComponent component ) {
		this.getContentPane().add( component );
		_toAddressField.requestFocus();
	}

	/**
	 * @see ComponentEditor#saveAttributes()
	 */
	public void saveAttributes() {
        data.setPlain( "toAddress", _toAddressField.getText() );
        data.setPlain( "subject", _subjectField.getText() );
        data.setPlain( "body", _editor.getText() );
	}

}
