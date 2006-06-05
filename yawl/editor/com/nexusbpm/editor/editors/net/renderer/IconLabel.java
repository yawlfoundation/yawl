package com.nexusbpm.editor.editors.net.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JLabel;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.nexusbpm.editor.persistence.DataProxy;

/**
 * This label displays the icons of components in the flow editor, based on their execution
 * status.
 * 
 * @author Dean Mao
 * @author Daniel Gredler
 * @created October 28, 2002
 * @version $Revision: 1.2 $
 */
public class IconLabel extends JLabel {

	private final static Log LOG = LogFactory.getLog( IconLabel.class );

	public final static Color EXECUTION_STATUS_COLOR_FINISHED_FILLING = new Color( 230, 230, 230 );
	public final static Color EXECUTION_STATUS_COLOR_NEW = Color.BLUE;
	public final static Color EXECUTION_STATUS_COLOR_RUNNING = Color.GREEN;
	public final static Color EXECUTION_STATUS_COLOR_UNKNOWN = Color.BLACK;
	public final static Color EXECUTION_STATUS_COLOR_ERROR = Color.RED;
	public final static Color EXECUTION_STATUS_COLOR_FINISHED = Color.GRAY;

	private DataProxy _proxy;

	/**
	 * Default constructor.
	 */
	public IconLabel() {
		setVerticalAlignment( JLabel.CENTER );
		setHorizontalAlignment( JLabel.CENTER );
		setHorizontalTextPosition( JLabel.CENTER );
		setVerticalTextPosition( JLabel.BOTTOM );
		setFont( UIManager.getFont( "Tree.font" ) );
		setForeground( UIManager.getColor( "Tree.textForeground" ) );
		setBackground( UIManager.getColor( "Tree.textBackground" ) );
	}

	/**
	 * Sets the proxy whose domain object's icon is to be displayed by this label. The icon to display
	 * is based on the domain object's execution status.
	 * @param proxy The proxy whose domain object's icon is to be displayed by this label.
	 * @throws CapselaException If there is an error retrieving the execution status from the domain object.
	 */
	public void setProxy( DataProxy proxy ) {
		_proxy = proxy;
		Object component = _proxy.getData();
		if( component != null ) {
			// The component is still around.
//			_executionStatus = component.getExecutionStatus();
			throw new RuntimeException("implement for yawl!");
		}
		else {
			// The component has been deleted.
//			_executionStatus = new ExecutionStatus( ExecutionStatus.STATUS_UNKNOWN, ExecutionStatus.REASON_UNKNOWN );
		}
		this.setIcon();
	}

	/**
	 * Sets this label's icon based on the current execution status.
	 */
	private void setIcon() {
//		if( _executionStatus.isFinished() ) {
//			this.setIcon( _proxy.iconGrayed() );
//		}
//		else if( _executionStatus.isError() ) {
//			this.setIcon( _proxy.iconError() );
//		}
//		else if( _executionStatus.isRunning() ) {
//			this.setIcon( _proxy.iconBright() );
//		}
//		else if( _executionStatus.isKilled() ) {
//			this.setIcon( _proxy.iconGrayed() );
//		}
//		else if( _executionStatus.isSuspended() ) {
//			this.setIcon( _proxy.iconBright() );
//		}
//		else if( _executionStatus.isNew() ) {
//			DefaultRenderer renderer = (DefaultRenderer) this.getParent();
//			if( renderer.getView().isMouseOverPort() == true ) {
//				this.setIcon( _proxy.iconBright() );
//			}
//			else {
//				this.setIcon( _proxy.iconLarge() );
//			}
//		}
		throw new RuntimeException("implement this for yawl");
	}

	/**
	 * @see java.awt.Component#paint(java.awt.Graphics)
	 */
	public void paint( Graphics g ) {
		this.setBackground( Color.white );
		this.setBorder( null );
		// Preview mode is "true" when we are dragging the component in the graph.
		DefaultRenderer renderer = (DefaultRenderer) this.getParent();
		if( ! renderer.isPreview() ) {
			// Only paint the component fully if we are not in preview mode.
			super.paint( g );
		}
		else {
			// This is how we will paint the component when we are in preview
			// mode (dragging component around).
			g.setColor( Color.BLACK );
			Dimension d = getSize();
			g.drawOval( ( d.width / 2 - 20 ), ( d.height / 2 - 20 ), 40, 40 );
		}
	}

}
