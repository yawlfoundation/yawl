/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence.dao;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.DatasourceFolder;

public class SpecificationSpringDAO extends AbstractSpringDAO<YSpecification> {

	private static final Log LOG = LogFactory.getLog( SpecificationSpringDAO.class );

	protected void preSave( YSpecification spec ) {
		try {
			spec.setID( new URI( spec.getID() ).toASCIIString() );
		}
		catch( URISyntaxException e ) {
			LOG.error( e );
		}
	}
	
	public Object getKey( YSpecification object ) {
		return PersistenceUtilities.getSpecificationDatabaseKey( object );
	}

	public List getChildren(Object parent) {
        List retval = new ArrayList();
        String filter = "";
        
        if( parent instanceof DatasourceFolder ) {
            DatasourceFolder folder = (DatasourceFolder) parent;
            filter = folder.getPath();
            if( ! filter.endsWith( "/" ) ) {
                filter = filter + "/";
            }
            
            List tmp = getHibernateTemplate().find( "from YSpecification spec where spec.ID like ?", filter + "%");
            
//            Set traversal = new HashSet( tmp );
            
            for( Object o : tmp ) {
                String id = getID( o );
                if( id != null && id.startsWith( filter ) ) {
                    if( PersistenceUtilities.contains( id, filter ) != null ) {
                        retval.add( new DatasourceFolder(
                        		PersistenceUtilities.contains( id, filter ), folder ) );
                    } else {
                        assert o instanceof YSpecification : "object not a specification";
                        retval.add( o );
                    }
                }
            }
        }
        

		
		LOG.debug("retrieving " + retval);
		return retval;
	}
	
	private String getID( Object object ) {
        if( object instanceof YSpecification ) {
            return ((YSpecification) object).getID();
        }
        else {
            return object.toString();
        }
    }
}
