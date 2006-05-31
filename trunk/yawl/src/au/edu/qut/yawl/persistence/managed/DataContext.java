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
import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * Holds value of property dao.
     */
    private DAO<YSpecification> dao;
    
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
    public DataProxy newObject(Object value, VetoableChangeListener listener) {
        try {
        } catch(Exception e){}
        return null;
    }

    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#get(java.io.Serializable)
	 */
    public Object get(Serializable key) {
    	return dao.retrieve(key);
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#put(Type)
	 */
    public void put(YSpecification t) {
    	dao.save(t);
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#remove(Type)
	 */
    public void remove(YSpecification t) {
    	dao.delete(t);
    }
    
    /* (non-Javadoc)
	 * @see au.edu.qut.yawl.persistence.managed.DataContext#getKeyFor(Type)
	 */
    public Serializable getKeyFor(YSpecification t) {
    	return dao.getKey(t);
    }
   
}
