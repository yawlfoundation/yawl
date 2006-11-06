/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.editors;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

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

	private JTextField _toAddressField;
    private JTextField _ccAddressField;
    private JTextField _bccAddressField;
    
	private JTextField _fromAddressField;
    
    private JTextField _hostField;
    
	private JTextField _subjectField;

    private JEditorPane _bodyEditor;
    
	private JButton _fileBrowseButton;
	private JTextField _fileAttachmentField;

	/**
	 * @see ComponentEditor#initializeUI()
	 */
	public JComponent initializeUI() throws EditorException {
		String toAddress = data.getPlain( "toAddress" );
		if( toAddress == null ) toAddress = "";
		_toAddressField = new JTextField( "", 30 );
		_toAddressField.setText( toAddress );
        
        String ccAddress = data.getPlain( "ccAddress" );
        if( ccAddress == null ) ccAddress = "";
        _ccAddressField = new JTextField( "", 30 );
        _ccAddressField.setText( ccAddress );
        
        String bccAddress = data.getPlain( "bccAddress" );
        if( bccAddress == null ) bccAddress = "";
        _bccAddressField = new JTextField( "", 30 );
        _bccAddressField.setText( bccAddress );
        
		String fromAddress = data.getPlain( "fromAddress" );
		if( fromAddress == null ) fromAddress = "";
		_fromAddressField = new JTextField( "", 30 );
		_fromAddressField.setText( fromAddress );
        
        String host = data.getPlain( "host" );
        if( host == null ) host = "";
        _hostField = new JTextField( "", 30 );
        _hostField.setText( host );
        
		String subject = data.getPlain( "subject" );
		if( subject == null ) subject = "";
		_subjectField = new JTextField( "", 30 );
		_subjectField.setText( subject );
        
		String body = data.getPlain( "body" );
		if( body == null ) body = "";
		_bodyEditor = new JEditorPane();
		_bodyEditor.setText( body );
        
//		FileAttribute attachment = _sender.getFileAttachment();
//		String attachmentFileName = ( attachment != null ? attachment.getDisplayedName() : "" );
		_fileAttachmentField = new JTextField();
//		_fileAttachmentField.setText( attachmentFileName );
        
		_fileBrowseButton = new JButton( "Browse" );
        
		addIsDirtyListener( _toAddressField );
        addIsDirtyListener( _ccAddressField );
        addIsDirtyListener( _bccAddressField );
		addIsDirtyListener( _fromAddressField );
        addIsDirtyListener( _hostField );
		addIsDirtyListener( _subjectField );
        addIsDirtyListener( _bodyEditor );
		addIsDirtyListener( _fileBrowseButton );
		addIsDirtyListener( _fileAttachmentField );
        
        final Component strut1 = Box.createVerticalStrut( 1 );
        final Component strut2 = Box.createVerticalStrut( 1 );
        final Component glue1 = Box.createHorizontalGlue();
        final Component glue2 = Box.createHorizontalGlue();
        final Component glue3 = Box.createHorizontalGlue();
        final Component glue4 = Box.createHorizontalGlue();
        
        final JPanel senderPanel = new JPanel();
        senderPanel.setLayout( new GridLayout( 2, 2 ) );
        senderPanel.setBorder( BorderFactory.createCompoundBorder(
                new TitledBorder(
                        BorderFactory.createBevelBorder( BevelBorder.LOWERED ),
                        "Sender information" ),
                BorderFactory.createEmptyBorder( 0, 3, 1, 3 ) ) );
        final JLabel hostLabel = new JLabel( "Host: " );
        senderPanel.add( hostLabel );
        senderPanel.add( _hostField );

        final JLabel fromLabel = new JLabel( "From Address: " );
        senderPanel.add( fromLabel );
        senderPanel.add( _fromAddressField );
        
        senderPanel.addMouseListener( new MouseAdapter() {
            boolean closed = false;
            @Override
            public void mouseClicked( MouseEvent e ) {
                if( e.getClickCount() == 2 ) {
                    if( closed ) {
                        senderPanel.remove( strut1 );
                        senderPanel.remove( glue1 );
                        senderPanel.remove( glue2 );
                        senderPanel.add( hostLabel );
                        senderPanel.add( _hostField );
                        senderPanel.add( fromLabel );
                        senderPanel.add( _fromAddressField );
                    }
                    else {
                        senderPanel.remove( hostLabel );
                        senderPanel.remove( _hostField );
                        senderPanel.remove( fromLabel );
                        senderPanel.remove( _fromAddressField );
                        senderPanel.add( strut1 );
                        senderPanel.add( glue1 );
                        senderPanel.add( glue2 );
                    }
                    senderPanel.invalidate();
                    senderPanel.getParent().getParent().validate();
                    closed = ! closed;
                }
            }
        });
        
        final JPanel recipientPanel = new JPanel();
        recipientPanel.setLayout( new GridLayout( 3, 2 ) );
        recipientPanel.setBorder( BorderFactory.createCompoundBorder(
                new TitledBorder(
                        BorderFactory.createBevelBorder( BevelBorder.LOWERED ),
                        "Recipient information" ),
                BorderFactory.createEmptyBorder( 0, 3, 1, 3 ) ) );
        final JLabel toLabel = new JLabel( "To Address: " );
        recipientPanel.add( toLabel );
        recipientPanel.add( _toAddressField );
        
        final JLabel ccLabel = new JLabel( "CC Address: " );
        recipientPanel.add( ccLabel );
        recipientPanel.add( _ccAddressField );
        
        final JLabel bccLabel = new JLabel( "BCC Address: " );
        recipientPanel.add( bccLabel );
        recipientPanel.add( _bccAddressField );
        
        recipientPanel.addMouseListener( new MouseAdapter() {
            boolean closed = false;
            @Override
            public void mouseClicked( MouseEvent e ) {
                if( e.getClickCount() == 2 ) {
                    if( closed ) {
                        recipientPanel.remove( strut2 );
                        recipientPanel.remove( glue3 );
                        recipientPanel.remove( glue4 );
                        recipientPanel.add( toLabel );
                        recipientPanel.add( _toAddressField );
                        recipientPanel.add( ccLabel );
                        recipientPanel.add( _ccAddressField );
                        recipientPanel.add( bccLabel );
                        recipientPanel.add( _bccAddressField );
                    }
                    else {
                        recipientPanel.remove( toLabel );
                        recipientPanel.remove( _toAddressField );
                        recipientPanel.remove( ccLabel );
                        recipientPanel.remove( _ccAddressField );
                        recipientPanel.remove( bccLabel );
                        recipientPanel.remove( _bccAddressField );
                        recipientPanel.add( strut2 );
                        recipientPanel.add( glue3 );
                        recipientPanel.add( glue4 );
                    }
                    recipientPanel.invalidate();
                    recipientPanel.getParent().getParent().validate();
                    closed = ! closed;
                }
            }
        });
        
        JPanel subjectPanel = new JPanel();
        subjectPanel.setLayout( new GridLayout( 1, 2 ) );
        subjectPanel.add( new JLabel( "Subject: " ) );
        subjectPanel.add( _subjectField );
        
        JPanel attachmentPanel = new JPanel();
        attachmentPanel.setLayout( new GridLayout( 1, 2 ) );
        attachmentPanel.add( _fileBrowseButton );
        attachmentPanel.add( _fileAttachmentField );
        
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout( new BorderLayout() );
//        messagePanel.setLayout( new BoxLayout( messagePanel, BoxLayout.Y_AXIS ) );
        messagePanel.setBorder( BorderFactory.createCompoundBorder(
                new TitledBorder(
                        BorderFactory.createBevelBorder( BevelBorder.LOWERED ),
                        "Message" ),
                BorderFactory.createEmptyBorder( 0, 3, 1, 3 ) ) );
        
        messagePanel.add( subjectPanel, BorderLayout.NORTH );
        messagePanel.add( new JScrollPane( _bodyEditor ), BorderLayout.CENTER );
        messagePanel.add( attachmentPanel , BorderLayout.SOUTH );
        
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
        
        JPanel ui = new JPanel();
        ui.setLayout( new BoxLayout( ui, BoxLayout.Y_AXIS ) );
		ui.setBorder( new EmptyBorder( 2, 3, 2, 3 ) );
        
        ui.add( senderPanel );
        ui.add( recipientPanel );
        ui.add( messagePanel );
        
//        ui.add( addressPanel, BorderLayout.NORTH );
//        ui.add( new JScrollPane( _bodyEditor ), BorderLayout.CENTER );
//        ui.add( attachmentPanel, BorderLayout.SOUTH );
        
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
        data.setPlain( "ccAddress", _ccAddressField.getText() );
        data.setPlain( "bccAddress", _bccAddressField.getText() );
        data.setPlain( "host", _hostField.getText() );
        data.setPlain( "subject", _subjectField.getText() );
        data.setPlain( "body", _bodyEditor.getText() );
	}

}
