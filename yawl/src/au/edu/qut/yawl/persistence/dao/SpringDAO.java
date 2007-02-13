/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.RestrictionCriterionConverter;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.util.ClassMap;

/**
 * A DAO that can save many kinds of objects, and will allow for customized
 * functionality to wrap the persistence (saving and deleting) of objects.
 * 
 * @author Nathan Rose
 */
public class SpringDAO extends HibernateDaoSupport implements DAO<Object> {
    private Map<Class, PersistenceWrapper> typeMap = new ClassMap<PersistenceWrapper>();
    
    private boolean dirty;
    
    public SpringDAO() {
//        System.err.println("constructor");
        addPersistenceWrapper(YIdentifier.class, new IdentifierPersistenceWrapper());
        addPersistenceWrapper(YSpecification.class, new SpecificationPersistenceWrapper());
    }
    
    public void addPersistenceWrapper(Class type, PersistenceWrapper wrapper) {
        typeMap.put(type, wrapper);
    }
    
    private boolean hasPersistenceWrapper(Class type) {
        return typeMap.get(type) != null;
    }
    
    private PersistenceWrapper<Object> getPersistenceWrapper(Class type) {
        return typeMap.get(type);
    }
    
    private void flushIfNeeded() {
        if(dirty) {
            getHibernateTemplate().flush();
            dirty = false;
        }
    }
    
    public Object retrieve(Class type, Object key) throws YPersistenceException {
        flushIfNeeded();
        return getHibernateTemplate().get(type, (Serializable) key);
    }
    
    public List<Object> retrieveByRestriction(Class type, Restriction restriction) throws YPersistenceException {
        flushIfNeeded();
        DetachedCriteria criteria = DetachedCriteria.forClass(type);
        
        /*
         * This is used because of a funny hibernate effect when using eager fetching, duplicate entries may be
         * retrieved. This ensures that only one of each are returned.
         */
        // criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        if(!(restriction instanceof Unrestricted)) {
            criteria.add(RestrictionCriterionConverter.convertRestriction(restriction));
        }
        
        Set<Object> set = new HashSet<Object>(getHibernateTemplate().findByCriteria(criteria));
        
        return new ArrayList<Object>(set);
    }
    
    public void save(Object object) throws YPersistenceException {
//        System.err.println("save " + getKey(object) + " (" + object.getClass() + ") " + object);
        if(object != null && hasPersistenceWrapper(object.getClass()))
            getPersistenceWrapper(object.getClass()).preSave(object, this);
        
        getHibernateTemplate().saveOrUpdate(object);
//        getHibernateTemplate().flush();
        dirty = true;
        
        if(object != null && hasPersistenceWrapper(object.getClass()))
            getPersistenceWrapper(object.getClass()).preSave(object, this);
    }
    
    public void delete(Object object) throws YPersistenceException {
//        System.err.println("delete " + getKey(object) + " (" + object.getClass() + ") " + object);
        if(object != null && hasPersistenceWrapper(object.getClass()))
            getPersistenceWrapper(object.getClass()).preDelete(object, this);
        
        getHibernateTemplate().delete(object);
//        getHibernateTemplate().flush();
        dirty = true;
        
        if(object != null && hasPersistenceWrapper(object.getClass()))
            getPersistenceWrapper(object.getClass()).postDelete(object, this);
    }
    
    public Object getKey(Object object) throws YPersistenceException {
        return PersistenceUtilities.getDatabaseKey(object);
    }
    
    public List getChildren(Object object) throws YPersistenceException {
        // TODO Auto-generated method stub
        return null;
    }
    
    public interface PersistenceWrapper<Type> {
        void preSave(Type object, SpringDAO dao) throws YPersistenceException;
        void postSave(Type object, SpringDAO dao) throws YPersistenceException;
        void preDelete(Type object, SpringDAO dao) throws YPersistenceException;
        void postDelete(Type object, SpringDAO dao) throws YPersistenceException;
    }
    
    public static abstract class AbstractPersistenceWrapper<Type> implements PersistenceWrapper<Type> {
        public void postDelete(Type object, SpringDAO dao) throws YPersistenceException {}
        public void postSave(Type object, SpringDAO dao) throws YPersistenceException {}
        public void preDelete(Type object, SpringDAO dao) throws YPersistenceException {}
        public void preSave(Type object, SpringDAO dao) throws YPersistenceException {}
    }
}
