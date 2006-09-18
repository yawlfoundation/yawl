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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.Parented;
import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
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
    public DataContext(DAO<Object> dao) {
        init(dao);
    }
    
    /** Creates a new instance of DataContext */
    public DataContext(DAO<Object> dao, Class dataProxyClass) {
        if (!DataProxy.class.isAssignableFrom(dataProxyClass)) {
        	throw new IllegalArgumentException(
        		dataProxyClass.getName() 
        		+ " is not a subclass of DataProxy");
        }        	
        this.dataProxyClass = dataProxyClass;
        LOG.trace( "DataContext initialized with proxy class " + dataProxyClass.getName() );
        init(dao);
    }

	/**
	 * @param dao
	 */
	private void init(DAO<Object> dao) {
		this.dao = dao;
        this.hierarchy = new HashBag<DataProxy,DataProxy>();
	}
    
    /**
     * Holds value of property dao.
     */
    private DAO<Object> dao;
    private Class dataProxyClass = DataProxy.class;
    /** Maps proxy objects to their data. */
    private Map<DataProxy, Object> dataMap = new HashMap<DataProxy, Object>();
    /** Maps data objects to their proxies. */
    private Map<Object, DataProxy> proxyMap = new HashMap<Object, DataProxy>();
    private HashBag<DataProxy,DataProxy> hierarchy;
    private Map<DataProxy, DataProxy> parents = new HashMap<DataProxy, DataProxy>();
    
    /**
     * Returns the data proxy for the given object. If there is no proxy for that
     * object in this data context then one will be created.
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getDataProxy(java.lang.Object)
	 */
    public DataProxy getDataProxy(Object dataObject) {
        return proxyMap.get(dataObject);
    }
    
    /**
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getData(DataProxy)
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
        assert object != null : "trying to create a proxy for null!";
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
            } else if( object instanceof DatasourceFolder ) {
                dataProxy.setLabel( ((DatasourceFolder) object).getName() );
            } else {
				dataProxy.setLabel(object.toString());
			}
            
			if (listener != null) dataProxy.addChangeListener(listener);
//            else LOG.warn( "Proxy for object '" + object + "' created with no listener!" );
			return dataProxy;
		} catch (Exception e) {throw new Error(e);//this can happen
		}
    }

    /**
	 * Retrieves the specification with the given key from the DAO and
     * generates proxies for the specification and its children.
	 */
    public DataProxy retrieve(Class type, Serializable key, DataProxyStateChangeListener listener) {
    	Object object = dao.retrieve(type, key);
    	// TODO how do we handle the parent proxy?
    	return handleRetrievedObject( object, null, listener );
    }
    
    public List<DataProxy> retrieveAll( Class type, DataProxyStateChangeListener listener ) {
    	return retrieveByRestriction( type, new Unrestricted(), null );
    }
    
    public List<DataProxy> retrieveByRestriction( Class type, Restriction restriction,
    		DataProxyStateChangeListener listener ) {
    	List<DataProxy> retval = new ArrayList<DataProxy>();
    	List objects = dao.retrieveByRestriction( type, restriction );
    	
    	for( Object o : objects ) {
    		// TODO how do we handle the parent proxy?
    		DataProxy proxy = handleRetrievedObject( o, null, listener );
    		if( proxy != null ) {
    			retval.add( proxy );
    		}
    	}
    	
    	return retval;
    }
    
    private DataProxy handleRetrievedObject( Object object, DataProxy parentProxy,
    		DataProxyStateChangeListener listener ) {
    	if( object != null && getDataProxy( object ) == null ) {
    		/*if( object instanceof YSpecification) {
                //WHAT IS THIS HERE FOR?????? - Should be moved to the client/editor
    			//RemoveNetConditionsOperation.removeConditions( (YSpecification) object );
                VisitSpecificationOperation.visitSpecification(
                        (YSpecification) object, getData( parentProxy ),
                        new Visitor() {
                            public void visit(Object child, Object parent, String childLabel) {
                                DataProxy childProxy;
                                if( DataContext.this.getDataProxy( child ) == null ) {
                                    childProxy = DataContext.this.createProxy(child, null);
                                    
                                    DataProxy parentProxy = DataContext.this.getDataProxy(parent);
                                    DataContext.this.attachProxy( childProxy, child, parentProxy );
                                }
                                else {
                                    // TODO FIXME this shouldn't really happen... but it does
                                    LOG.warn( "Revisiting child:" + child );
                                    childProxy = DataContext.this.getDataProxy( child );
                                }
                                if( childLabel == null ) {
                                    childLabel = "null";
                                }
                                childProxy.setLabel(childLabel);
                            }
                        });
        	}*/
    		//else {
    			
    		//}
    		
    		DataProxy proxy = createProxy( object, listener );
			attachProxy( proxy, object, parentProxy );
    	}
    	
    	return getDataProxy( object );
    }
    
    /**
     * Tells the DAO to save the object that the proxy is a proxy for,
     * if the proxy is in the context.
	 */
    public void save(DataProxy dataProxy) throws YPersistenceException {
    	Object object = getData( dataProxy );
		//System.out.println("saving " + object );
		if( object == null ) {
			throw new YPersistenceException("Cannot persist null as an object!" );
		}
        dao.save( object );
    }
    
    /**
	 * Tells the DAO to delete the object that the proxy is a proxy for
     * and removes the proxy and data from the context.
     * TODO remove the detach proxy part
	 */
    public void delete(DataProxy dataProxy) {
        Object data = getData(dataProxy);
        DataProxy parent = getParentProxy(dataProxy);
        
        dao.delete( data );
        detachProxy(dataProxy, data, parent);
    }
    
    /**
	 * @see au.edu.qut.yawl.persistence.dao.DAO#getKey(Object)
	 */
    public Serializable getKeyFor(DataProxy t) {
    	return (Serializable) dao.getKey(getData(t));
    }
    
    /**
     * Attaches the given proxy and the object that it's a proxy for back to
     * the data context. It is necessary to pass both the proxy and the
     * object because the proxy delegates retrieving its data to the context
     * (this class) which won't have its data until after it's attached.
     * @param dataProxy the proxy to add to the context.
     * @param object the data object that the proxy is a proxy for.
     */
    public void attachProxy(DataProxy dataProxy, Object object, DataProxy parent) {
        dataProxy.fireAttaching(object, parent);
        
        dataProxy.setContext( this );
        addToMaps(dataProxy, object);
        addToHierarchy(dataProxy, parent);
        
        dataProxy.fireAttached(object, parent);
    }
    
    /**
     * Detaches a data proxy from the context.
     * @param dataProxy the proxy object to detach.
     */
    public void detachProxy(DataProxy dataProxy, Object object, DataProxy parent) {
        dataProxy.fireDetaching(object, parent);
        
        removeFromHierarchy(dataProxy, parent);
        removeFromMaps(dataProxy, object);
        dataProxy.setContext(null);
        
        dataProxy.fireDetached(object, parent);
    }
    
    public DataProxy getParentProxy(DataProxy child) {
        return parents.get( child );
    }
    
    /**
     * Adds the child proxy to the hierarchy as a child of the given parent proxy.
     * @param childProxy
     */
    private void addToHierarchy(DataProxy childProxy, DataProxy parentProxy) {
        parents.put( childProxy, parentProxy );
        if( parentProxy != null ) {
            hierarchy.put(parentProxy, childProxy);
        }
    }
    
    /**
     * Removes the child proxy, and all of its children, from the hierarchy.
     */
    private void removeFromHierarchy(DataProxy childProxy, DataProxy parentProxy) {
        parents.remove( childProxy );
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
   
    public Set<DataProxy> getChildren(DataProxy parent, boolean forceUpdate) {
    	if (hierarchy.get(parent) == null || forceUpdate) {
    		List<Parented> l = dao.getChildren(parent.getData());
    		for (Object o: l) {
                assert o != null : "data proxy returned a null child!" + parent.toString();
                handleRetrievedObject( o, parent, null );
//    			if (o instanceof YSpecification) {
//                    RemoveNetConditionsOperation.removeConditions( (YSpecification) o );
//                    VisitSpecificationOperation.visitSpecification(
//                            (YSpecification) o, getData( parent ),
//                            new Visitor() {
//                                public void visit(Object child, Object parent, String childLabel) {
//                                    DataProxy childProxy;
//                                    if( DataContext.this.getDataProxy( child ) == null ) {
//                                        childProxy = DataContext.this.createProxy(child, null);
//                                        
//                                        DataProxy parentProxy = DataContext.this.getDataProxy(parent);
//                                        DataContext.this.attachProxy( childProxy, child, parentProxy );
//                                    }
//                                    else {
//                                        // TODO FIXME this shouldn't really happen... but it does
//                                        LOG.warn( "Revisiting child:" + child );
//                                        childProxy = DataContext.this.getDataProxy( child );
//                                    }
//                                    if( childLabel == null ) {
//                                        childLabel = "null";
//                                    }
//                                    childProxy.setLabel(childLabel);
//                                }
//                            });
//    			}
//    			else {
//    				DataProxy dp = createProxy(o, null);
//                    attachProxy(dp, o, parent);
//    			}
    		}
    	}
    	else {
    	}
    	return hierarchy.get(parent);
    }
}
