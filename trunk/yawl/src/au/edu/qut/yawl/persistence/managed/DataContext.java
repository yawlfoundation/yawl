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

import au.edu.qut.yawl.elements.YDecomposition;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YFlow;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;



/**
 * 
 * @author SandozM
 */
public class DataContext {
    
    /** Creates a new instance of DataContext */
    public DataContext(DAO<YSpecification> dao) {
        this.dao = dao;
    }
    
    /** Creates a new instance of DataContext */
    public DataContext(DAO<YSpecification> dao, Class dataProxyClass) {
        this.dao = dao;
        if (!DataProxy.class.isAssignableFrom(dataProxyClass)) {
        	throw new IllegalArgumentException(
        		dataProxyClass.getName() 
        		+ " is not a subclass of DataProxy");
        }        	
        this.dataProxyClass = dataProxyClass;
    }
    
    /**
     * Holds value of property dao.
     */
    private DAO<YSpecification> dao;
    private Class dataProxyClass = DataProxy.class;
    private Map<DataProxy, Object> dataMap = new HashMap<DataProxy, Object>();
    private Map<Object, DataProxy> proxyMap = new HashMap<Object, DataProxy>();
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getDataProxy(java.lang.Object)
	 */
    public DataProxy getDataProxy(Object dataObject) {
        if (!proxyMap.containsKey(dataObject)) {
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
    protected DataProxy newObject(Object o, VetoableChangeListener listener) {
		try {
			DataProxy dp = (DataProxy) dataProxyClass.newInstance();
			dp.setContext(this);
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
    public DataProxy get(Serializable key) {
    	DataProxy dp = newObject(dao.retrieve(key), null);
    	return dp;
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#put(Type)
	 */
    public void put(DataProxy t) {
    	dao.save((YSpecification) dataMap.get(t));
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
   
    /**
     * This should enumerate some of the extra controllers we might need
     * but leaves out the question of how to connect them - via parent
     * and or child relationships etc. as well as what connection *means*
     * for example if a flow points to another task is the flow the parent?
     * this could lead to traversal issues...best sit back and think about 
     * it a bit...
     */ 

    public void generateSubcontrollers(YSpecification spec) {
    	List<YDecomposition> decomps = spec.getDecompositions();
    	for (YDecomposition decomp: decomps) {
    		newObject(decomp, null);
    		if (decomp instanceof YNet) {
    			YNet net = (YNet) decomp;
    			newObject(net.getInputCondition(), null);
    			newObject(net.getOutputCondition(), null);
    			for(YExternalNetElement yene: net.getNetElementsDB()) {
    				newObject(yene, null);
    				Collection<YFlow> flows = yene.getPostsetFlows();
    				for (YFlow flow: flows) {
    					newObject(flows, null);
    				}
    				flows = yene.getPresetFlows();
    				for (YFlow flow: flows) {
    					newObject(flows, null);
    				}
    			}
    		}
    	}
    }
}
