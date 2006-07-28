/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.managed;
/*
 * DataContext.java
 *
 * Created on April 20, 2006, 5:24 PM
 */

import java.beans.VetoableChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.Parented;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.util.HashBag;
import au.edu.qut.yawl.util.RemoveNetConditionsOperation;
import au.edu.qut.yawl.util.VisitSpecificationOperation;
import au.edu.qut.yawl.util.VisitSpecificationOperation.Visitor;

/**
 * 
 * @author Matthew Sandoz
 * @author Nathan Rose
 */
public class DataContext {
	private static final Log LOG = LogFactory.getLog(DataContext.class);

    /** Creates a new instance of DataContext */
    public DataContext(DAO<YSpecification> dao) {
        init(dao);
    }
    
    /** Creates a new instance of DataContext */
    public DataContext(DAO<YSpecification> dao, Class dataProxyClass) {
        if (!DataProxy.class.isAssignableFrom(dataProxyClass)) {
        	throw new IllegalArgumentException(
        		dataProxyClass.getName() 
        		+ " is not a subclass of DataProxy");
        }        	
        this.dataProxyClass = dataProxyClass;
        init(dao);
    }

	/**
	 * @param dao
	 */
	private void init(DAO<YSpecification> dao) {
		this.dao = dao;
        this.hierarchy = new HashBag<DataProxy,DataProxy>();
	}
    
    /**
     * Holds value of property dao.
     */
    private DAO<YSpecification> dao;
    private Class dataProxyClass = DataProxy.class;
    /** Maps proxy objects to their data. */
    private Map<DataProxy, Object> dataMap = new HashMap<DataProxy, Object>();
    /** Maps data objects to their proxies. */
    private Map<Object, DataProxy> proxyMap = new HashMap<Object, DataProxy>();
    private HashBag<DataProxy,DataProxy> hierarchy;
    
    /**
     * Returns the data proxy for the given object. If there is no proxy for that
     * object in this data context then one will be created.
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getDataProxy(java.lang.Object)
	 */
    public DataProxy getDataProxy(Object dataObject, VetoableChangeListener listener) {
        return proxyMap.get(dataObject);
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getData(au.edu.qut.yawl.persistence.managed.DataProxy)
	 */
    public Object getData(DataProxy proxyObject) {
        assert dataMap.containsKey(proxyObject) : "attempting to access the data from a disconnected proxy";
        return dataMap.get(proxyObject);
    }
    
    /**
     * Creates a data proxy for the given data object, assuming that the
     * object does not already have a proxy.
	 */
    public DataProxy createProxy(Object object, DataProxyStateChangeListener listener) {
		try {
			DataProxy dataProxy = (DataProxy) dataProxyClass.newInstance();
			dataProxy.setContext(this);
            
			if (object instanceof YExternalNetElement) {
				YExternalNetElement netElement = (YExternalNetElement) object;
				if (netElement.getName() == null || netElement.getName().length() == 0) {
					dataProxy.setLabel(netElement.getID());
				} else {
					dataProxy.setLabel(netElement.getName());
				}
			} else if (object instanceof YDecomposition) {
                YDecomposition netElement = (YDecomposition) object;
                if (netElement.getName() == null || netElement.getName().length() == 0) {
                    dataProxy.setLabel(netElement.getId());
                } else {
                    dataProxy.setLabel(netElement.getName());
                }
            } else if( object instanceof YSpecification ) {
                YSpecification spec = (YSpecification) object;
                if( spec.getName() == null || spec.getName().length() == 0 ) {
                    dataProxy.setLabel(spec.getID());
                }
                else {
                    dataProxy.setLabel(spec.getName());
                }
            } else {
				dataProxy.setLabel(object.toString());
			}
            
			if (listener != null) dataProxy.addChangeListener(listener);
//            else LOG.warn( "Proxy for object '" + object + "' created with no listener!" );
			return dataProxy;
		} catch (Exception e) {throw new Error(e);//this can never happen
		}
    }

//    /**
//	 * Retrieves the specification with the given key from the DAO and
//     * generates proxies for the specification and its children.
//	 */
//    public DataProxy retrieve(Serializable key, VetoableChangeListener listener) {
//    	YSpecification spec = dao.retrieve(key);
//    	if (spec != null) this.generateProxies(spec);
//    	return getDataProxy(spec, listener);
//    }
    
    /**
     * Tells the DAO to save the object that the proxy is a proxy for,
     * if the proxy is in the context.
	 */
    public void save(DataProxy dataProxy) {
    	YSpecification spec = (YSpecification) getData(dataProxy);
		System.out.println("saving " + spec);
        
		if (spec != null) {
	    	dao.save((YSpecification) dataMap.get(dataProxy));
		}
    }
    
    /**
	 * Tells the DAO to delete the object that the proxy is a proxy for
     * and removes the proxy and data from the context.
	 */
    public void delete(DataProxy dataProxy) {
    	Object data = getData(dataProxy);
    	if (data instanceof YSpecification) {
    		dao.delete((YSpecification) data);
    	}
        removeFromMaps(dataProxy, data);
        removeFromHierarchy(dataProxy);
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getKeyFor(Type)
	 */
    public Serializable getKeyFor(DataProxy t) {
    	return dao.getKey((YSpecification) dataMap.get(t));
    }
    
    /**
     * Attaches the given proxy and the object that it's a proxy for back to
     * the data context. It is necessary to pass both the proxy and the
     * object because the proxy delegates retrieving its data to the context
     * (this class) which won't have its data until after it's attached.
     * <em>If the object does not implement Parented, then it will not be
     * connected to the hierarchy.</em>
     * @param dataProxy the proxy to add to the context.
     * @param object the data object that the proxy is a proxy for.
     */
    public void attachProxy(DataProxy dataProxy, Object object, DataProxy parent) {
        dataProxy.setContext( this );
        addToMaps(dataProxy, object);
        addToHierarchy(dataProxy);
        dataProxy.fireAttached(object, parent);
    }
    
    /**
     * Detaches a data proxy from the context.
     * @param dataProxy the proxy object to detach.
     */
    public void detachProxy(DataProxy dataProxy) {
        Object object = dataProxy.getData();
        dataProxy.fireDetached(object, getParentProxy( dataProxy ) );
        
        removeFromHierarchy(dataProxy);
        removeFromMaps(dataProxy, object);
        
        dataProxy.setContext(null);
    }
    
    public DataProxy getParentProxy(DataProxy child) {
        if( child.getData() != null
                && child.getData() instanceof Parented
                && ((Parented)child.getData()).getParent() != null ) {
            return proxyMap.get( ((Parented)child.getData()).getParent() );
        }
        else if( child.getData() != null
                && child.getData() instanceof YSpecification ) {
//            LOG.warn( "ID of spec:" + ((YSpecification)child.getData()).getID() );
            String id = ((YSpecification)child.getData()).getID();
            if( id.replaceAll( "\\\\", "/" ).indexOf( "/" ) >= 0 ) {
                id = id.substring( 0, id.replaceAll( "\\\\", "/" ).lastIndexOf( "/" ) );
//                LOG.warn( "new id: " + id + " returns " + getDataProxy( id, null ) );
                return getDataProxy( id, null );
            }
            else {
                return null;
            }
        }
//        LOG.warn( "returning null parent for proxy " + child.getLabel() );
        return null;
    }
    
    /**
     * The child proxy must be attached to the context for this function to work.
     * If the child does not implement Parented, then the child won't be added.
     * @param childProxy
     */
    private void addToHierarchy(DataProxy childProxy) {
        if( getParentProxy( childProxy ) != null ) {
            hierarchy.put(getParentProxy( childProxy ), childProxy);
//            LOG.info("adding " + childProxy.getLabel() + " to " + getParentProxy( childProxy ).getLabel());
        }
    }
    
    /**
     * Removes the child proxy, and all of its children, from the hierarchy.
     * <em>If the object does not implement parented, then it will not be
     * removed from the hierarchy.</em>
     */
    private void removeFromHierarchy(DataProxy childProxy) {
        DataProxy parentProxy = getParentProxy( childProxy );
        if( parentProxy != null && hierarchy.get( parentProxy ) != null ) {
            hierarchy.get( parentProxy ).remove( childProxy );
//            LOG.info("removing " + childProxy.getLabel() + " from " + getParentProxy( childProxy ).getLabel());
        }
        hierarchy.remove( childProxy );
//        LOG.info("removing " + childProxy.getLabel() + " as a parent");
    }
    
    private void addToMaps(DataProxy proxy, Object data) {
        dataMap.put(proxy, data);
        proxyMap.put(data, proxy);
//        LOG.info("adding " + proxy.getLabel() + " to maps.");
    }
    
    private void removeFromMaps(DataProxy proxy, Object data) {
        dataMap.remove(proxy);
        proxyMap.remove(data);
//        LOG.info("removing " + proxy.getLabel() + " from maps.");
    }
    
    public void renameFolder( DataProxy folder, String name ) {
        // TODO temporary, somewhat-working solution. NEEDS to be redone
        Object oldVal = dataMap.get( folder );
        removeFromMaps( folder, oldVal );
        addToMaps( folder, name );
    }
   
    public Set<DataProxy> getChildren(DataProxy parent, boolean forceUpdate) {
    	if (hierarchy.get(parent) == null || forceUpdate) {
    		List<Parented> l = dao.getChildren(parent.getData());
    		for (Object o: l) {
                assert o != null : "data proxy returned a null child!" + parent.toString();
    			if (o instanceof YSpecification) {
                    RemoveNetConditionsOperation.removeConditions( (YSpecification) o );
                    VisitSpecificationOperation.visitSpecification(
                            (YSpecification) o,
                            new Visitor() {
                                public void visit(Object child, String childLabel) {
                                    DataProxy childProxy;
                                    if( DataContext.this.getDataProxy( child, null ) == null ) {
                                        childProxy = DataContext.this.createProxy(child, null);
                                        DataContext.this.attachProxy( childProxy, child, null );
                                    }
                                    else {
                                        childProxy = DataContext.this.getDataProxy( child, null );
                                    }
                                    childProxy.setLabel(childLabel);
                                }
                            });
                    hierarchy.put(parent, getDataProxy( o, null ) );
    			}
    			else {
    				DataProxy dp = createProxy(o, null);
                    attachProxy(dp, o, parent);
        			hierarchy.put(parent, dp);
    			}
    		}
    	}
    	else {
    	}
    	return hierarchy.get(parent);
    }
    
    public static void removeConditions(YSpecification spec) {
    	for (YDecomposition decomp: spec.getDecompositions()) {
    		if (decomp instanceof YNet) {
    			List<YExternalNetElement> copySet = new ArrayList<YExternalNetElement>();
    			copySet.addAll(((YNet) decomp).getNetElements());
    			for (YExternalNetElement currentElement: copySet) {
    				if (currentElement.getClass() == YCondition.class) {
    					List<YExternalNetElement> elementsBeforeCondition = currentElement.getPresetElements();
    					List<YExternalNetElement> elementsAfterCondition = currentElement.getPostsetElements();
    					if (elementsBeforeCondition.size() == 1) {
    						YExternalNetElement elementBeforeCondition = elementsBeforeCondition.get(0);
    						for (YExternalNetElement elementAfterCondition: elementsAfterCondition) {
    							for (YFlow flow: elementAfterCondition.getPresetFlows()) {
    								if (flow.getPriorElement() == currentElement) {
    									flow.setPriorElement(elementAfterCondition);
    								}
    							}
    							for (YFlow flow: elementBeforeCondition.getPostsetFlows()) {
    								if (flow.getNextElement() == currentElement) {
    									flow.setNextElement(elementAfterCondition);
    								}
    							}
    							
    						}
    					}
    					((YNet) decomp).getNetElements().remove(currentElement);
    				}
    			}
    		}
    	}
    }

}
