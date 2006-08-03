package com.nexusbpm.editor.editors;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YNet;

import com.nexusbpm.editor.editors.net.GraphEditor;
import com.nexusbpm.editor.exception.EditorException;
import com.nexusbpm.editor.persistence.EditorDataProxy;

/**
 * A net editor which can contain multiple tabs, each of which will contain a
 * <code>GraphEditor</code>.
 * 
 * @see        com.ichg.capsela.client.editors.flow.GraphEditor
 * @author     catch23
 * @created    October 28, 2002
 */
public class NetEditor extends ComponentEditor {

	private final static Log LOG = LogFactory.getLog( NetEditor.class );

	private GraphEditor _netGraphEditor;
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

	/** Set of proxys for net instances that have tabs in this editor. */
//	private HashSet<EditorDataProxy> proxyList = new HashSet<EditorDataProxy>();
    private Map<EditorDataProxy,GraphEditor> proxyToGraphMap = new HashMap<EditorDataProxy,GraphEditor>();

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
     * @see com.nexusbpm.editor.editors.ComponentEditor#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange( PropertyChangeEvent event ) {
        super.propertyChange( event );
        _netGraphEditor.propertyChange( event );
    }

    /**
	 * @see ComponentEditor#setUI(JComponent)
	 */
	protected void setUI( JComponent component ) {
		this.getContentPane().add( component );
	}

	/**
	 * @see ComponentEditor#saveAttributes()
	 */
	public void saveAttributes() {
		if( _netGraphEditor != null ) {
			_netGraphEditor.saveAttributes();
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
			_netGraphEditor = ge;
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
	 * Removes the graph editor for the specified instance net.
	 * @param proxy the proxy for the net instance whose
	 *              tab is to be removed from this net editor.
	 */
	public void removeGraphEditor( GraphEditor editor ) {
		if( null != _tabbedPane && null != editor ) {
            _tabbedPane.remove( editor );
            try {
                // TODO XXX
                editor.clear();
            }
            catch( Throwable t ) {
                LOG.error( "Error disposing of net editor instance graph!", t );
            }
		}
	}

	/**
	 * Returns the non-instance flow graph editor, if any. If there is none,
	 * this method returns <tt>null</tt>.
	 * @return the <tt>GraphEditor</tt> for the flow template or <tt>null</tt>.
	 */
	public GraphEditor getNetGraphEditor() {
		return _netGraphEditor;
	}

	/**
	 * Removes the graph editors from this flow editor and their tabs.
	 * @see CapselaInternalFrame#frameClosed()
	 */
	public void frameClosed() throws Exception {
        LOG.debug( "NetEditor.frameClosed()" );

        if( null != proxyToGraphMap ) {
            // remove GraphEditors from instance proxys
            Set<EditorDataProxy> tmp = new HashSet<EditorDataProxy>( proxyToGraphMap.keySet() );
            for( EditorDataProxy proxy : tmp ) {
                GraphEditor editor = proxyToGraphMap.get( proxy );
                proxyToGraphMap.remove( proxy );
                removeGraphEditor( editor );
            }
            proxyToGraphMap = null;
        }

        if( null != _netGraphEditor ) {
            _netGraphEditor.removeEverything();
            removeGraphEditor( _netGraphEditor );
            _netGraphEditor = null;
        }

        if( null != _tabbedPane ) {
            if( null != instanceEditor ) {
                _tabbedPane.remove( instanceEditor );
            }
            getContentPane().remove( _tabbedPane ); // 2005-05-13 14:35
            _tabbedPane.removeAll();
            _tabbedPane = null; // mjf 2005-05-13 9:43
        }

        try {
        if( null != instanceEditor ) {
            GraphEditor tmp = instanceEditor;
            instanceEditor = null;
            tmp.clear();
        }

        if( !isDirty() ) {
            clearing();
        }
        }
        catch( Throwable t ) {
            throw new Exception( t );
        }

        cleared = true;
        super.frameClosed();
    }

	/**
	 * Called from clear as well as from saveAttributes after a clear.
	 * @throws Throwable
	 */
	private void clearing() throws Throwable {
		LOG.debug("FlowEditor.clearing");
		if (null != _netGraphEditor) {
			GraphEditor tmp = _netGraphEditor;
			_netGraphEditor = null;
			removeIsDirtyListener(tmp);
			tmp.clear();
		}

		_tabbedPane = null;

		if( null != _proxy ) {
			_proxy.removeChangeListener( this );
		}
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
