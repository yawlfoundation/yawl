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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
import au.edu.qut.yawl.persistence.dao.SpecificationHibernateDAO;
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
    private Map<DataProxy, Object> dataMap = new HashMap<DataProxy, Object>();
    private Map<Object, DataProxy> proxyMap = new HashMap<Object, DataProxy>();
    private HashBag<DataProxy> hierarchy;    
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getDataProxy(java.lang.Object)
	 */
    public DataProxy getDataProxy(Object dataObject, VetoableChangeListener listener) {
        if (!proxyMap.containsKey(dataObject)) {
        	newObject(dataObject, listener);
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
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#newObject(Type, java.beans.VetoableChangeListener)
	 */
    private DataProxy newObject(Object o, VetoableChangeListener listener) {
		try {
			DataProxy dp = (DataProxy) dataProxyClass.newInstance();
			dp.setContext(this);
			if (o instanceof YExternalNetElement) {
				YExternalNetElement ne = (YExternalNetElement) o;
				if (ne.getName() == null || ne.getName().length() == 0) {
					dp.setLabel(ne.getID());
				} else {
					dp.setLabel(ne.getName());
				}
			} else {
				dp.setLabel(o.toString());
			}
			if (listener != null) dp.addVetoableChangeListener(listener); 
			dataMap.put(dp, o);
			proxyMap.put(o, dp);
			return dp;
		} catch (Exception e) {return null;//this can never happen
		}
    }
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#newObject(Type, java.beans.VetoableChangeListener)
	 */
    public DataProxy newObject(Class type, VetoableChangeListener listener) {
    	DataProxy retval = null;
    	try {
    		Object o = type.newInstance();
    		retval = this.newObject(o, listener);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return retval;
    }

    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#get(java.io.Serializable)
	 */
    public DataProxy get(Serializable key, VetoableChangeListener listener) {
    	YSpecification spec = dao.retrieve(key);
    	if (spec != null) this.generateProxies(spec);
    	return getDataProxy(spec, listener);
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#put(Type)
	 */
    public void put(DataProxy dp) {
    	YSpecification spec = (YSpecification)dp.getData();
    	LOG.info("x2DECOMPS=" + spec.getDecompositions().size());
		for (YDecomposition k: spec.getDecompositions()) {
			LOG.info(k.getName() + "x2:" + k.getParent());
		}
		dataMap.put(dp, dp.getData());
		if (dp.getData() != null) {
	    	dao.save((YSpecification) dataMap.get(dp));
			proxyMap.put(dp.getData(), dp);
		}

    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#remove(Type)
	 */
    public void remove(DataProxy t) {
    	Object data = dataMap.get(t);
    	dao.delete((YSpecification) data);
    	dataMap.remove(t);
    	proxyMap.remove(data);
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getKeyFor(Type)
	 */
    public Serializable getKeyFor(DataProxy t) {
    	return dao.getKey((YSpecification) dataMap.get(t));
    }
   
    public Set<DataProxy> getChildren(DataProxy parent) {
    	if (hierarchy.get(parent) == null) {
    		List l = dao.getChildren(parent.getData());
    		for (Object o: l) {
    			if (o instanceof YSpecification) {
    				generateProxies((YSpecification) o);
    				hierarchy.put(parent, getDataProxy(o, null));
    			}
    			else {
    				DataProxy dp = newObject(o, null);
        			hierarchy.put(parent, dp);
    			}
    		}
    	}
    	return hierarchy.get(parent);
    }
    
    public void generateProxies(YSpecification spec) {
		assert spec != null;
    	DataProxy specProxy = newObject(spec, null);
    	specProxy.setLabel(spec.getName());
    	List<YDecomposition> decomps = spec.getDecompositions();
    	for (YDecomposition decomp: decomps) {
    		DataProxy decompProxy = newObject(decomp, null);
    		if (decomp.getName() != null && decomp.getName().length() != 0) {
    			decompProxy.setLabel(decomp.getName());
    		} else {
    			decompProxy.setLabel(decomp.getId());
    		}
    		hierarchy.put(specProxy, decompProxy);
    		if (decomp instanceof YNet) {
    			YNet net = (YNet) decomp;
    			for(YExternalNetElement yene: net.getNetElementsDB()) {
    				DataProxy netElementProxy = newObject(yene, null);
    				if (findType(yene) == Type.CONDITION) {
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
    			for(YExternalNetElement yene: net.getNetElementsDB()) {
        			Collection<YFlow> flows = yene.getPostsetFlows();
    				for (YFlow flow: flows) {
    					DataProxy flowProxy = newObject(flow, null);
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
    					DataProxy flowProxy = newObject(flow, null);
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

}
