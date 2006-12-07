/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.events;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.persistence.dao.restrictions.RestrictionCriterionConverter;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;

public class SpringEventDispatcher extends HibernateDaoSupport implements EventDispatcher, DAO<Event> {
	protected SpringEventDispatcher() {
	}
	
	public void fireEvent( Event event ) {
		save( event );
	}
	
	public List getChildren( Object object ) {
		return null;
	}

	public final void save( Event object ) {
		getHibernateTemplate().merge( object );
	}
	
	public final Event retrieve( Class Event, Object key ) {
		return (Event) getHibernateTemplate().get( Event, (Serializable) key );
	}
	
	public List<Event> retrieveByRestriction( Class Event, Restriction restriction ) {
		DetachedCriteria criteria = DetachedCriteria.forClass( Event );
		
		if( ! ( restriction instanceof Unrestricted ) ) {
        	criteria.add( RestrictionCriterionConverter.convertRestriction( restriction ) );
        }
		
		return getHibernateTemplate().findByCriteria( criteria );
	}
	
	public final void delete( Event object ) {
		Event persistedObject = (Event) getHibernateTemplate().get(
				object.getClass(), (Serializable) getKey( object ) );
		assert persistedObject != null : "attempting to delete object that wasn't persisted";
		// TODO which way do we want this?
//		if( persistedObject == null ) {
//			return false;
//		}
		getHibernateTemplate().delete( persistedObject );
	}

	public Object getKey( Event event ) {
		return event.getId();
	}
}
