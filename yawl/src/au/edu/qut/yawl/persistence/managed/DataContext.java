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
import java.util.HashMap;
import java.util.Map;

import au.edu.qut.yawl.persistence.dao.DAO;



/**
 * 
 * @author SandozM
 */
public class DataContext<Type> {
    
    /** Creates a new instance of DataContext */
    public DataContext(DAO<Type> dao) {
        this.dao = dao;
        mapper = new ContextDataMapper();
    }
    
    /**
     * Holds value of property dao.
     */
    private DAO<Type> dao;
    private ContextDataMapper mapper;
    
    public ManagedDataObject getManagedObject(Object key, VetoableChangeListener listener) {
        if (!mapper.contains(key)) {
            ManagedDataObject managedData = new ManagedDataObject(key, mapper, listener);
            Type value = dao.retrieve(key);
            MapperElement n = new MapperElement(value, managedData);
            mapper.cache.put(key, n);
        }
        return mapper.getManagedObject(key);
    }
    
    public ManagedDataObject createManagedObject(Type value, VetoableChangeListener listener) {
        ManagedDataObject managedData = new ManagedDataObject(value, mapper, listener);
        try {
            MapperElement<Type> n = new MapperElement<Type>(value, managedData);
            dao.save(value);
            mapper.cache.put(dao.getKey(value), n);
        } catch(Exception e){}
        return managedData;
    }

    public DAO getDao() {
        return dao;
    }

    public void setDao(DAO val) {
        this.dao = val;
    }

    public ContextDataMapper getMapper() {
        return mapper;
    }

    public void setMapper(ContextDataMapper val) {
        this.mapper = val;
    }
    
    private static class ContextDataMapper<Type> implements KeyedDataMapper {
        
        private Map<Object, MapperElement<Type>> cache = new HashMap<Object, MapperElement<Type>>();
        public Type getData(Object key) {return cache.get(key).data;}
        public Map getMetadata(Object key) {return cache.get(key).metadata;}
        public boolean contains(Object key) {return cache.containsKey(key);}
        protected ManagedDataObject getManagedObject(Object key) {return cache.get(key).managed;}

        public Map<Object,MapperElement<Type>> getCache() {
            return cache;
        }

        public void setCache(Map<Object,MapperElement<Type>> val) {
            this.cache = val;
        }
    }
    static class MapperElement<Type>{
    	Type data;
        Map metadata = new HashMap();
        ManagedDataObject managed;
        public MapperElement(Type data, ManagedDataObject managed) {
            this.data = data;
            this.managed = managed;
        }
    }
}
