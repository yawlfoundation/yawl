/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;

@Stateless
@Remote(DelegatedEJB3DAO.class)
public class DelegatedEJB3DAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedEJB3DAO.class );
	
	@PersistenceContext
	private EntityManager manager;
	
	public DelegatedEJB3DAO() {
		addType( YSpecification.class, new SpecificationEJB3DAO() );
		addType( YNetRunner.class, new NetRunnerEJB3DAO() );
	}
	
	private abstract class AbstractEJB3DAO<Type> implements DAO<Type> {
		public final void delete( Type object ) {
			manager.remove( object );
		}

		public final Type retrieve( Class type, Object key ) {
			return (Type) manager.find( type, key );
		}
		
		public List<Type> retrieveByRestriction( Class type, Restriction restriction ) {
			// TODO
			return new ArrayList<Type>();
		}

		public final void save( Type object ) {
			manager.merge( object );
		}
		
		public List getChildren( Object object ) {
			return new ArrayList();
		}
	}

	private class SpecificationEJB3DAO extends AbstractEJB3DAO<YSpecification> {
		public Object getKey( YSpecification object ) {
			return PersistenceUtilities.getSpecificationKey( object );
		}
		
		public List getChildren( Object object ) {
			if( object == null ) throw new IllegalArgumentException( "Parent can not be null" );
			List retval = new ArrayList();
			// XXX FIXME TODO
//			if( object instanceof DatasourceFolder ) {
//				DatasourceFolder parent = (DatasourceFolder) object;
//				String path = parent.getPath();
//				if( !path.endsWith( "/" ) ) {
//					path = path + "/";
//				}
//				for( YSpecification spec : objects.values() ) {
//					String id = spec.getSpecURI();
//					if( id != null && id.startsWith( path ) ) {
//						if( PersistenceUtilities.contains( id, path ) != null ) {
//							retval.add( new DatasourceFolder(
//									PersistenceUtilities.contains( id, path ), parent ) );
//						}
//						else {
//							retval.add( spec );
//						}
//					}
//				}
//			}
			return retval;
		}
	}

	private class NetRunnerEJB3DAO extends AbstractEJB3DAO<YNetRunner> {
		public Object getKey( YNetRunner object ) {
			return PersistenceUtilities.getNetRunnerKey( object );
		}
	}
}
