/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package com.nexusbpm.editor.desktop;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;
import javax.swing.event.InternalFrameListener;
import javax.swing.text.JTextComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.icon.ApplicationIcon;
import com.nexusbpm.editor.icon.RenderingHints;

/**
 * Base class for all internal frames shown on the Capsela desktop.
 * 
 * This class has to implement <tt>DropTargetListener</tt>, because drop events
 * fall through the Z-order to the first Component that has an installed listener.
 * This means that if internal frames don't have drop listeners, and there is
 * an editor underneath (even if not visible) that has a drop listener, Swing
 * will use that listener and allow a drop. So in Capsela, if you have a flow editor
 * underneath a sql component editor, you will be able to drop a component anywhere
 * in the sql component editor, and it will fall through to the flow editor. See
 * http://java.sun.com/j2se/1.4/jcp/j2se-1_4-beta2-mr_docs-spec/ in the section titled
 * "DnD Event Targeting Update".
 * 
 * @author HoY
 * @author Daniel Gredler
 */
public class CapselaInternalFrame extends JInternalFrame
implements DropTargetListener
{
	private static final Log LOG = LogFactory.getLog( CapselaInternalFrame.class );

	/** The border size to use between components. */
	public final static int BORDER = 5;
	/** The height to use for a horizontal line of things on a GUI. */
	public final static int LINE_HEIGHT = 26;
	/**
	 * Dimension to use as preferred size when a component should expand to fill
	 * the space available to it.
	 */
	public final static Dimension EXPAND_TO_FILL_AVAILABLE_SPACE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

	private static final ImageIcon LOADING_ICON = ApplicationIcon.getIcon("NexusInternalFrame.load_animation", RenderingHints.ICON_LARGE);
	private static final String LOADING_MSG = "Loading data, please wait...";

	private Container _originalContentPane;
	/** Label used when an editor is loading. */
	protected JLabel _loadingLabel;

	/**
	 * Default constructor.
	 */
	public CapselaInternalFrame() {

		super();

		this.setClosable(true);
		this.setMaximizable(true);
		this.setResizable(true);
		this.setIconifiable(false);

		this.setSize(300, 300);

		this.setFrameIcon(ApplicationIcon.getIcon("NexusInternalFrame.window_icon"));

		new DropTarget(this, this);

		_loadingLabel = new JLabel(LOADING_MSG, LOADING_ICON, JLabel.CENTER);
		_loadingLabel.setHorizontalTextPosition(JLabel.CENTER);
		_loadingLabel.setVerticalTextPosition(JLabel.BOTTOM);
		_loadingLabel.setBackground(Color.WHITE);

		this.addLoadingLabel();
	}

	/**
	 * Adds the specified component to the editor's content pane and centers it.
	 * @param component the component to center in the editor's content pane
	 */
	protected void setCenteredComponent(JComponent component) {
		this.setCenteredComponent(component, null);
	}

	/**
	 * Adds the specified component to the editor's content pane and centers it.
	 * @param component the component to center in the editor's content pane
	 * @param c the color to make the content pane
	 */
	protected void setCenteredComponent(JComponent component, Color c) {
		int w = component.getWidth();
		int h = component.getHeight();
		this.setCenteredComponent(component, w, h, c);
	}

	/**
	 * Adds the specified component to the editor's content pane and centers it.
	 * @param component the component to center in the editor's content pane
	 * @param width the width to make the component (if <= 0, then it is not applied)
	 * @param height the height to make the component (if <= 0, then it is not applied)
	 * @param c the color to make the content pane
	 */
	protected void setCenteredComponent(JComponent component, int width, int height, Color c) {

		if( width > 0 && height > 0 ) {
			Dimension d = new Dimension(width, height);
			component.setPreferredSize(d);
			component.setMinimumSize(d);
			component.setMaximumSize(d);
		}

		JPanel pane = new JPanel();
		pane.setBorder(new EmptyBorder(BORDER, BORDER, BORDER, BORDER));
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		pane.add(Box.createVerticalGlue());
		pane.add(component);
		pane.add(Box.createVerticalGlue());
		if( c != null ) {
			pane.setBackground(c);
		}

		JPanel pane2 = new JPanel();
		pane2.setLayout(new BoxLayout(pane2, BoxLayout.X_AXIS));
		pane2.add(Box.createHorizontalGlue());
		pane2.add(pane);
		pane2.add(Box.createHorizontalGlue());
		if( c != null ) {
			pane2.setBackground(c);
		}

		this.setContentPane(pane2);
	}

	/**
	 * Adds the loading label to the frame.
	 */
	public void addLoadingLabel() {
		_originalContentPane = getContentPane();
		this.setCenteredComponent(_loadingLabel, Color.WHITE);
	}

	/**
	 * Removes the loading label from the frame.
	 */
	public void removeLoadingLabel() {
		if (null != _originalContentPane) {
			setContentPane(_originalContentPane);
			_originalContentPane = null;
		}
	}

	/**
	 * Recursively disables or enables all input elements in the specified
	 * container, except for the exempt components.
	 * @param c the container whose input elements should be enabled or disabled
	 * @param exempt an array of components that are exempt
	 * @param enabled whether the input elements should be enabled or disabled
	 */
	protected void setInputElementsEnabled( Container c, Component[] exempt, boolean enabled ) {
	    boolean isContainerExempt = contains( exempt, c );
	    if( isContainerExempt ) return;
		Component[] components = c.getComponents();
		for( int i = 0; i < components.length; i++ ) {
			Component component = components[i];
			boolean isExempt = contains( exempt, component );
			if( ! isExempt ) {
				// don't disable DefaultRenderer because it gets reused
				// by JGraph (flywheel pattern)
//				if( ! ( component instanceof DefaultRenderer ) ) {  TODO XXX uncomment later when we import the jgraph stuff
					if( component instanceof JTextComponent ||
							component instanceof JComboBox ||
							component instanceof JSlider ||
							component instanceof JSpinner ||
							component instanceof JEditorPane ||
							component instanceof JLabel ||
							component instanceof AbstractButton) {
						component.setEnabled( enabled );
					}
					if( component instanceof Container ) {
						setInputElementsEnabled( (Container) component, exempt, enabled );
					}
//				}
			}
		}
	}

	/**
	 * Returns <tt>true</tt> if the specified component array contains the
	 * specified component.
	 */
	private boolean contains( Component[] components, Component component ) {
	    if( components == null ) return false;
		for( int j = 0; j < components.length; j++ ) {
			if( components[j] == component ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Empty implementation.
	 * @see DropTargetListener#dragEnter(DropTargetDragEvent)
	 */
	public void dragEnter(DropTargetDragEvent dtde) {
		// Empty.
	}

	/**
	 * Empty implementation.
	 * @see DropTargetListener#dragOver(DropTargetDragEvent)
	 */
	public void dragOver(DropTargetDragEvent dtde) {
		// Empty.
	}

	/**
	 * Empty implementation.
	 * @see DropTargetListener#dropActionChanged(DropTargetDragEvent)
	 */
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// Empty.
	}

	/**
	 * Empty implementation.
	 * @see DropTargetListener#drop(DropTargetDropEvent)
	 */
	public void drop(DropTargetDropEvent dtde) {
		// Empty.
	}

	/**
	 * Empty implementation.
	 * @see DropTargetListener#dragExit(DropTargetEvent)
	 */
	public void dragExit(DropTargetEvent dte) {
		// Empty.
	}

	/**
	 * Remove all frame listeners from this frame.
     * @deprecated listeners should not be removed in bulk, use instead
     * {@link JInternalFrame#removeInternalFrameListener(InternalFrameListener)}.
	 */
	public void removeInternalFrameListeners() {
		InternalFrameListener listeners[] = getInternalFrameListeners();
		if( listeners.length > 0 ) {
			LOG.debug("CapselaInternalFrame.removeEditorFrameListeners Removing " + listeners.length + " listeners");
			for( int i = 0; i < listeners.length; i++ ) {
				removeInternalFrameListener( listeners[ i ] );
			}
		}
	}

	/**
	 * Called by the ComponentEditorFrameListener when the frame has been closed
     * and is safe to dispose of.
	 * @throws Exception so subclasses can throw exceptions.
	 */
	public void frameClosed() throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("CapselaInternalFrame.clear " + getClass().getName());
		}

		_loadingLabel = null;
		_originalContentPane = null;

		dispose();
	}
}
