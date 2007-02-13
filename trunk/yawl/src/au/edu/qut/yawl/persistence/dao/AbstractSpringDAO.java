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
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.RestrictionCriterionConverter;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;



public abstract class AbstractSpringDAO<Type> extends HibernateDaoSupport implements DAO<Type> {
	/**
	 * Hook for subclassers to take care of any operation necessary before the object is saved.
	 */
	protected abstract void preSave( Type object ) throws YPersistenceException;
	
	public void save( Type object ) throws YPersistenceException {
		preSave( object );

		getHibernateTemplate().saveOrUpdate( object );
		getHibernateTemplate().flush();
	}
	
	public final Type retrieve( Class type, Object key ) {
		return (Type) getHibernateTemplate().get( type, (Serializable) key );
	}
	
	public List<Type> retrieveByRestriction( Class type, Restriction restriction ) {
		DetachedCriteria criteria = DetachedCriteria.forClass( type );
		
		/*
		 * This is used because of a funny hibernate effect
		 * when using eager fetching, duplicate entries may
		 * be retrieved. This ensures that only one of each are returned.
		 * */
		//criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
		
		
		if( ! ( restriction instanceof Unrestricted ) ) {
			criteria.add( RestrictionCriterionConverter.convertRestriction( restriction ) );
		}
		
		criteria.addOrder(Order.asc(""));

		Set<Type> set = new HashSet<Type>( getHibernateTemplate().findByCriteria( criteria ) );
		
		return new ArrayList<Type>( set );
	}
	
	public final void delete( Type object ) {


		//Type persistedObject = (Type) getHibernateTemplate().get(
		//		object.getClass(), (Serializable) getKey( object ) );
		//assert persistedObject != null : "attempting to delete object that wasn't persisted";
		// TODO which way do we want this?
//		if( persistedObject == null ) {
//			return false;
//		}


		getHibernateTemplate().delete( object );
		getHibernateTemplate().flush();
	}
	
	public List getChildren( Object object ) {
		return new ArrayList();
	}
}