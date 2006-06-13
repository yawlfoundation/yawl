package com.nexusbpm.editor.editors.net.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgraph.JGraph;
import org.jgraph.graph.CellView;
import org.jgraph.graph.CellViewRenderer;

import com.nexusbpm.editor.editors.net.cells.CapselaCell;
import com.nexusbpm.editor.editors.net.cells.DefaultView;
import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.persistence.EditorDataProxy;


/**
 * This is for "Zubin's long description under the icon" renderer. Puts a
 * paragraph under flow components when in the flow graph.
 * 
 * 
 * @author Dean Mao
 * @created Jun 15, 2004
 */
public class DefaultRenderer extends JComponent implements CellViewRenderer {

	private final static Log LOG = LogFactory.getLog( DefaultRenderer.class );

	private EditorDataProxy _proxy;
	private NameLabel _nameLabel;
	private IconLabel _iconLabel;
	private DescriptionTextArea _descriptionTextArea;

	private DefaultView _view;
	private boolean _isSelected;
	private boolean _isFocused;
	private boolean _isPreview;

	/**
	 * Default constructor.
	 */
	public DefaultRenderer() {

		_iconLabel = new IconLabel();
		_descriptionTextArea = new DescriptionTextArea( "text/html", "" );
		_descriptionTextArea.setOpaque( true );
		_iconLabel.setOpaque( false );
		_nameLabel = new NameLabel();

		GridBagLayout gbl = new GridBagLayout();
		setLayout( gbl );

		GridBagConstraints nameLabelConstraints = new GridBagConstraints();
		nameLabelConstraints.gridx = 0;
		nameLabelConstraints.gridy = 0;
		nameLabelConstraints.gridwidth = 1;
		nameLabelConstraints.gridheight = 1;
		nameLabelConstraints.weightx = 1;
		nameLabelConstraints.weighty = 0;
		nameLabelConstraints.anchor = GridBagConstraints.SOUTH;
		nameLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		nameLabelConstraints.insets = new Insets( 0, 0, 3, 0 );
		this.add( _nameLabel, nameLabelConstraints );

		GridBagConstraints iconLabelConstraints = new GridBagConstraints();
		iconLabelConstraints.gridx = 0;
		iconLabelConstraints.gridy = 1;
		iconLabelConstraints.gridwidth = 1;
		iconLabelConstraints.gridheight = 1;
		iconLabelConstraints.weightx = 1;
		iconLabelConstraints.weighty = 0;
		iconLabelConstraints.anchor = GridBagConstraints.CENTER;
		iconLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
		this.add( _iconLabel, iconLabelConstraints );

		GridBagConstraints descriptionConstraints = new GridBagConstraints();
		descriptionConstraints.gridx = 0;
		descriptionConstraints.gridy = 2;
		descriptionConstraints.gridwidth = 1;
		descriptionConstraints.gridheight = 1;
		descriptionConstraints.weightx = 1;
		descriptionConstraints.weighty = 1;
		descriptionConstraints.anchor = GridBagConstraints.NORTH;
		descriptionConstraints.fill = GridBagConstraints.BOTH;
		descriptionConstraints.insets = new Insets( 3, 0, 0, 0 );
		this.add( _descriptionTextArea, descriptionConstraints );
	}

	/**
	 * @see JComponent#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		dim.setSize( dim.getWidth() + 40, dim.getHeight() );
		return dim;
	}

	/**
	 * @see CellViewRenderer#getRendererComponent(org.jgraph.JGraph, org.jgraph.graph.CellView, boolean, boolean, boolean)
	 */
	public java.awt.Component getRendererComponent( JGraph graph, CellView view, boolean sel, boolean focus, boolean preview ) {
		if( view instanceof DefaultView ) {
			DefaultView defaultView = (DefaultView) view;
			if( graph.getEditingCell() != defaultView.getCell() ) {
				setBackground( Color.blue );
			}
			_view = defaultView;
			_isSelected = sel;
			_isFocused = focus;
			_isPreview = preview;
			try {
				EditorDataProxy proxy = ( (CapselaCell) defaultView.getCell() ).getProxy();
				_nameLabel.setProxy( proxy );
				_iconLabel.setProxy( proxy );
				_descriptionTextArea.setProxy( proxy );
				_proxy = proxy;
			}
			catch( EditorException e ) {
				// CapselaExceptions are already logged.
			}
			return this;
		}
		return null;
	}

	public DefaultView getView() {
		return _view;
	}

	public boolean isSelected() {
		return _isSelected;
	}

	public boolean isFocused() {
		return _isFocused;
	}

	public boolean isPreview() {
		return _isPreview;
	}

	public IconLabel getNameRenderer() {
		return _iconLabel;
	}

}