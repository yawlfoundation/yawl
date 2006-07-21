package com.nexusbpm.editor.editors;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YNet;

import com.nexusbpm.editor.editors.net.GraphEditor;
import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * A flow editor which can contain multiple tabs, each of which will contain a
 * <code>GraphEditor</code>.
 * 
 * @see        com.ichg.capsela.client.editors.flow.GraphEditor
 * @author     catch23
 * @created    October 28, 2002
 */
public class NetEditor extends ComponentEditor {

	private final static Log LOG = LogFactory.getLog( NetEditor.class );

	private GraphEditor _flowGraphEditor;
	private JTabbedPane _tabbedPane;
	/**
	 * save this off so we can clear it later
	 */
	private GraphEditor instanceEditor = null;

	private boolean cleared = false;

	/**
	 * Default constructor.
	 */
	public NetEditor() {
		super();
	}

	/** Set of proxys for flow instances that have tabs in this editor. */
	private HashSet<EditorDataProxy> proxyList = new HashSet<EditorDataProxy>();

	/**
	 * @see ComponentEditor#initializeUI()
	 */
	public JComponent initializeUI() throws EditorException {

		_tabbedPane = new JTabbedPane();
		addGraphEditor( _proxy, true);

		// XXX TODO here we check if it is an instance or not...  and initialize the graph differently if it is.
//		Object object = _proxy.getData();
//		Boolean isInstance = (Boolean) object.getAttribute( com.ichg.capsela.domain.component.Component.ATTR_INSTANCE );
//		if( isInstance.booleanValue() == false ) {
//			DataProxy instanceCtrl = ClientOperation.latestInstance( _proxy );
//			if( instanceCtrl != null ) {
//				proxyList.add( instanceCtrl );
//				addGraphEditor( instanceCtrl, false );
//			}
//		}

		return _tabbedPane;
	}

	/**
	 * @see ComponentEditor#setUI(JComponent)
	 */
	protected void setUI( JComponent component ) {
		this.getContentPane().add( component );
	}

	/**
	 * Override this method so as to increase its visibility and allow outside
	 * graph change listeners to modify the dirty flag.
	 * @param isDirty whether the component has been modified.
	 * @see ComponentEditor#setDirty(boolean)
	 */
	public void setDirty( boolean isDirty ) {
		super.setDirty( isDirty );
	}

	/**
	 * @see ComponentEditor#saveAttributes()
	 */
	public void saveAttributes() {
		if( _flowGraphEditor != null ) {
			_flowGraphEditor.saveAttributes();
		}

		if (cleared) {
			try {
				clearing();
			}
			catch( Throwable throwable ) {
				LOG.warn( "saveAttributes", throwable);
			}
		}
	}

	/**
	 * Adds a graph editor for the specified flow to this flow editor.
	 * @param proxy the proxy for the flow template or instance to
	 *                   create a graph editor for.
	 * @param focusNewTab whether the new tab should be given focus or not.
	 */
	public void addGraphEditor( EditorDataProxy proxy, final boolean focusNewTab) throws EditorException {
		// Get the flow from the cache.
		final YNet flow = (YNet) proxy.getData();
		if (null == flow) {
			throw new EditorException("FlowEditor.addGraphEditor - missing flow - fnt=" + focusNewTab);
		}
//		boolean isInstance = flow.isInstance();
		boolean isInstance = false; // TODO XXX deal with instances later when we figure out how to represent them in yawl
		if( LOG.isDebugEnabled() ) {
			String type = ( isInstance ? "instance" : "template" );
			LOG.debug( "FlowEditor.addGraphEditor " + type + " proxy: " + proxy.toString() + ", focusNewTab: " + focusNewTab );
		}
		// Create and initialize the new graph editor.
		GraphEditor ge = new GraphEditor( isInstance , this);
		if( isInstance ) {
			instanceEditor = ge;
			ge.initializeInstance( proxy );
		}
		else {
			_flowGraphEditor = ge;
			ge.setProxy( proxy );
			ge.initialize( proxy.getTreeNode() );
		}
		// Figure out what tab title the new editor will be using.
		String tabTitle;
//		if( isInstance ) {
//			Date start = ( flow.getStartTime() == 0 ? new Date() : new Date( flow.getStartTime() ) );
//			tabTitle = INSTANCE_DATE_FORMAT.format( start );
//		}
//		else {
			tabTitle = "Flow";
//		}
		// Actually add the new graph editor to the flow editor's tab pane.
		_tabbedPane.add( tabTitle, ge );
		if( focusNewTab ) _tabbedPane.setSelectedComponent( ge );
		ge.refresh(flow);
		if( isInstance ) {
			// TODO: HACK: Resume animation sequence if there are components in the running state
			throw new RuntimeException("figure this part out!");
//			for( Iterator i = flow.getComponents().iterator(); i.hasNext(); ) {
//				com.ichg.capsela.domain.component.Component c = (com.ichg.capsela.domain.component.Component) i.next();
//				DataProxy ctrl = (DataProxy) DomainObjectproxy.getproxy( c );
//				if( c.getExecutionStatus().isRunning() ) {
//					instanceEditor.startCellAnimation( ctrl );
//					LOG.debug( "Starting cell animation for: " + ctrl.toString() );
//				}
//			}
		}
		else {
            // TODO there is no change listener for a graph editor
			addIsDirtyListener( ge );
		}
	}

	/**
	 * Removes the graph editor for the specified flow.
	 * @param proxy the proxy for the flow template or instance whose
	 *                   tab is to be removed from this flow editor.
	 */
	public void removeGraphEditor( EditorDataProxy proxy ) {
		if( null != _tabbedPane && null != proxy ) {
			Component[] components = _tabbedPane.getComponents();
			int removed = 0;
			for( int i = 0; i < components.length; i++ ) {
				Component component = components[ i ];
				GraphEditor editor = (GraphEditor) component;
				if( proxy.equals( editor.getProxy() ) ) {
					_tabbedPane.remove( editor );
					try {
						editor.clear();
					}
					catch( Throwable throwable ) {
						LOG.warn("FlowEditor.removeGraphEditor", throwable);
					}
					break;
				}
			}

			LOG.debug("FlowEditor.removeGraphEditor removed " + removed + " of " + components.length);
		}
	}

	/**
	 * Returns the non-instance flow graph editor, if any. If there is none,
	 * this method returns <tt>null</tt>.
	 * @return the <tt>GraphEditor</tt> for the flow template or <tt>null</tt>.
	 */
	public GraphEditor getFlowGraphEditor() {
		return _flowGraphEditor;
	}

	/**
	 * Removes the graph editors from this flow editor and their tabs.
	 * @see ComponentEditor#clear()
	 */
	public void clear() throws Throwable {
		LOG.debug("FlowEditor.debug");

		if (null != proxyList) {
			// remove GraphEditors from instance proxys
			HashSet<EditorDataProxy> tmp = proxyList;
			proxyList = null;
			EditorDataProxy tmpproxy;
			for (Iterator<EditorDataProxy> iterator = tmp.iterator(); iterator.hasNext(); ) {
				tmpproxy = iterator.next();
				removeGraphEditor(tmpproxy);
			}
			if (null != tmp)
				LOG.debug("removing graph editors from " + tmp.size() + " proxys ");
		}

		if (null != _proxy) {
			removeGraphEditor(_proxy);
		}

		if (null != _tabbedPane) {
			if (null != instanceEditor) {
				_tabbedPane.remove( instanceEditor);
			}
			getContentPane().remove( _tabbedPane ); // 2005-05-13 14:35
			_tabbedPane.removeAll();
			_tabbedPane = null; // mjf 2005-05-13 9:43
		}

		if (null != instanceEditor) {
			GraphEditor tmp = instanceEditor;
			instanceEditor = null;
			tmp.clear();
		}

		if (!isDirty()) {
			clearing();
		}

		cleared = true;
		super.clear();
	}

	/**
	 * Called from clear as well as from saveAttributes after a clear.
	 * @throws Throwable
	 */
	private void clearing() throws Throwable {
		LOG.debug("FlowEditor.clearing");
		if (null != _flowGraphEditor) {
			GraphEditor tmp = _flowGraphEditor;
			_flowGraphEditor = null;
			removeIsDirtyListener(tmp);
			tmp.clear();
		}

		_tabbedPane = null;

		if( null != _proxy ) {
			_proxy.removeChangeListener( this );
			_proxy = null;
		}
	}

	/**
	 * Logs that this flow editor is being finalized and calls {@link #clear()}.
	 * @see ComponentEditor#finalize()
	 */
	public void finalize() throws Throwable {
		LOG.debug("FlowEditor.finalize");
		clear();
		super.finalize();
	}

	/**
	 * Overriding the ComponentEditor version with an empty implementation so
	 * that instances are not disabled (greyed out).
	 * @see com.ichg.capsela.client.editors.ComponentEditor#disableInputElementsIfInstance(java.awt.Component[])
	 */
	protected void disableInputElementsIfInstance( Component[] exempt ) throws EditorException {
		// empty implementation
	}
}
