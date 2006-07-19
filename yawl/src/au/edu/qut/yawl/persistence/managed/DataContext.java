package au.edu.qut.yawl.persistence.managed;
/*
 * DataContext.java
 *
 * Created on April 20, 2006, 5:24 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import java.beans.VetoableChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YOutputCondition;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.util.HashBag;


/**
 * 
 * @author Matthew Sandoz
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
        this.hierarchy = new HashBag<DataProxy>();
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
    private HashBag<DataProxy> hierarchy;    
    
    /**
     * Returns the data proxy for the given object. If there is no proxy for that
     * object in this data context then one will be created.
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getDataProxy(java.lang.Object)
	 */
    public DataProxy getDataProxy(Object dataObject, VetoableChangeListener listener) {
        if (!proxyMap.containsKey(dataObject)) {
        	createProxy(dataObject, listener);
        }
        return proxyMap.get(dataObject);
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getData(au.edu.qut.yawl.persistence.managed.DataProxy)
	 */
    public Object getData(DataProxy proxyObject) {
        if (!dataMap.containsKey(proxyObject)) {
        }
        return dataMap.get(proxyObject);
    }
    
    /**
     * Creates a data proxy for the given data object, assuming that the
     * object does not already have a proxy.
	 */
    private DataProxy createProxy(Object object, VetoableChangeListener listener) {
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
			} else {
				dataProxy.setLabel(object.toString());
			}
            
			if (listener != null) dataProxy.addVetoableChangeListener(listener); 
			attachProxy(dataProxy, object);
			return dataProxy;
		} catch (Exception e) {return null;//this can never happen
		}
    }

    /**
	 * Retrieves the specification with the given key from the DAO and
     * generates proxies for the specification and its children.
	 */
    public DataProxy retrieve(Serializable key, VetoableChangeListener listener) {
    	YSpecification spec = dao.retrieve(key);
    	if (spec != null) this.generateProxies(spec);
    	return getDataProxy(spec, listener);
    }
    
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
    	dao.delete((YSpecification) data);
    	dataMap.remove(dataProxy);
    	proxyMap.remove(data);
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
     * @param dataProxy the proxy to add to the context.
     * @param object the data object that the proxy is a proxy for.
     */
    public void attachProxy(DataProxy dataProxy, Object object) {
        dataProxy.setContext( this );
        dataMap.put(dataProxy, object);
        proxyMap.put(object, dataProxy);
        
        Object parent = ((Parented) object).getParent();
        DataProxy parentProxy = getDataProxy( parent, null );
        hierarchy.put( parentProxy, dataProxy );
    }
    
    /**
     * Detaches a data proxy from the context.
     * @param dataProxy the proxy object to detach.
     */
    public void detachProxy(DataProxy dataProxy) {
        Parented object = (Parented) dataProxy.getData();
        Object parent = object.getParent();
        DataProxy parentProxy = getDataProxy( parent, null );
        dataMap.remove(dataProxy);
        proxyMap.remove(object);
        
        hierarchy.remove( dataProxy );
        hierarchy.get( parent ).remove( dataProxy );
        
        dataProxy.setContext( null );
    }
   
    public Set<DataProxy> getChildren(DataProxy parent, boolean forceUpdate) {
    	if (hierarchy.get(parent) == null || forceUpdate) {
    		List l = dao.getChildren(parent.getData());
    		for (Object o: l) {
    			if (o instanceof YSpecification) {
    				generateProxies((YSpecification) o);
    				hierarchy.put(parent, getDataProxy(o, null));
    			}
    			else {
    				DataProxy dp = createProxy(o, null);
        			hierarchy.put(parent, dp);
    			}
    		}
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
    
    
    public void generateProxies(YSpecification spec) {
		assert spec != null;
		removeConditions(spec);
    	DataProxy specProxy = createProxy(spec, null);
    	specProxy.setLabel(spec.getName());
    	List<YDecomposition> decomps = spec.getDecompositions();
    	for (YDecomposition decomp: decomps) {
    		DataProxy decompProxy = createProxy(decomp, null);
    		if (decomp.getName() != null && decomp.getName().length() != 0) {
    			decompProxy.setLabel(decomp.getName());
    		} else {
    			decompProxy.setLabel(decomp.getId());
    		}
    		hierarchy.put(specProxy, decompProxy);
    		if (decomp instanceof YNet) {
    			YNet net = (YNet) decomp;
    			for(YExternalNetElement yene: net.getNetElements()) {
    				DataProxy netElementProxy = createProxy(yene, null);
    				if (findType(yene) == Type.CONDITION) {
    					LOG.error(yene.getClass().getName());
    					netElementProxy.setLabel("{connector}");
    				} else {
    					if (yene.getName() != null && yene.getName().length() != 0) {
        					netElementProxy.setLabel(yene.getName());
    					}else {
        					netElementProxy.setLabel(yene.getID());
    					}
    				}
        			hierarchy.put(decompProxy, netElementProxy);
    			}
    		}
    	}
    	for (YDecomposition decomp: decomps) {
    		if (decomp instanceof YNet) {
    			YNet net = (YNet) decomp;
    			for(YExternalNetElement yene: net.getNetElements()) {
        			Collection<YFlow> flows = yene.getPostsetFlows();
    				for (YFlow flow: flows) {
    					DataProxy flowProxy = createProxy(flow, null);
    					String to;
        				if (findType(flow.getNextElement()) == Type.CONDITION) {
        					to = "to connector";
        				} else {
        					to = "to " + getDataProxy(flow.getNextElement(), null).getLabel();
        				}
    					flowProxy.setLabel(to);
    	    			hierarchy.put(getDataProxy(yene, null), flowProxy);
    				}
    				flows = yene.getPresetFlows();
    				for (YFlow flow: flows) {
    					DataProxy flowProxy = createProxy(flow, null);
    					String from;
        				if (findType(flow.getPriorElement()) == Type.CONDITION) {
        					from = "from connector";
        				} else {
        					from = "from " + getDataProxy(flow.getPriorElement(), null).getLabel();
        				}
    					flowProxy.setLabel(from);
    					hierarchy.put(getDataProxy(yene, null), flowProxy);
    				}
    			}
    		}
    	}
    }

    //these should all be moved elsewhere!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    
    private enum Type {UNKNOWN, INPUT_CONDITION, OUTPUT_CONDITION, TASK, CONDITION, FLOW, SPECIFICATION};
    private Type findType (Object o) {
    	Type retval = Type.UNKNOWN;
    	if (o instanceof YInputCondition ) {retval = Type.INPUT_CONDITION;}
    	else if (o instanceof YOutputCondition) {retval = Type.OUTPUT_CONDITION;}
    	else if (o instanceof YTask) {retval = Type.TASK;}
    	else if (o instanceof YCondition) {retval = Type.CONDITION;}
    	else if (o instanceof YFlow) {retval = Type.FLOW;}
    	else if (o instanceof YSpecification) {retval = Type.SPECIFICATION;}
    	return retval;
    }

	public HashBag<DataProxy> getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(HashBag<DataProxy> hierarchy) {
		this.hierarchy = hierarchy;
	}

}
