/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.unmarshal.YMarshal;

public class DelegatedFileDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog( DelegatedFileDAO.class );
	
	public DelegatedFileDAO() {
		addType( YSpecification.class, new SpecificationFileDAO() );
//		addType( YNetRunner.class, new NetRunnerFileDAO() );
	}
	
	public List getChildren( Object object ) {
		return getDAOForType( YSpecification.class ).getChildren( object );
	}
	
	private abstract class AbstractFileDAO<Type> implements DAO<Type> {
		/* cache the loaded objects so we're not constantly going back out to the file system. */
		protected Map<Object, Type> loadedObjects = new HashMap<Object, Type>();
		protected Map<Type, Object> reverseMap = new HashMap<Type, Object>();
		
		protected void cache( Object key, Type object ) {
			loadedObjects.put( key, object );
			reverseMap.put( object, key );
		}
		
		protected void uncache( Type object ) {
			Object key = reverseMap.get( object );
			reverseMap.remove( object );
			loadedObjects.remove( key );
		}
		
		public List<Type> retrieveByRestriction( Class type, Restriction restriction ) {
			// restrictions don't make sense in the context of a hierarchical filesystem the
			// same way they do for a database or memory
			throw new UnsupportedOperationException(
					"The file system DAO does not support retrieval by restriction!" );
		}
	}
	
	private class SpecificationFileDAO extends AbstractFileDAO<YSpecification> {
		public Object getKey( YSpecification object ) {
			return PersistenceUtilities.getSpecificationKey( object );
		}
		
		public void save( YSpecification spec ) {
			File f = null;
			try {
				if( !spec.getID().toLowerCase().endsWith( ".xml" ) ) spec.setID( spec.getID() + ".xml" );
				LOG.info( "saving " + spec.getID() );
				f = new File( new URI( spec.getID() ) );
				if( f.getParentFile() != null ) f.getParentFile().mkdirs();
				spec.setID( new URI( spec.getID() ).toASCIIString() );
				FileWriter os = new FileWriter( f );
				os.write( YMarshal.marshal( spec ) );
				os.flush();
				os.close();
				String key = new File( spec.getID() ).toURI().toASCIIString();
				cache( key, spec );
			}
			catch( Exception e ) {
				LOG.error( "error saving " + spec.getID() + " to file", e );
			}
		}
		
		public YSpecification retrieve( Class type, Object key ) {
			if( key == null ) { return null; }
			URI specURI = new File( key.toString() ).toURI();
			String specLocation = specURI.toASCIIString();
			if( loadedObjects.containsKey( specLocation ) ) return loadedObjects.get( specLocation );
			try {
				LOG.info( "retrieving file spec " + specLocation );
				List l = null;
				l = YMarshal.unmarshalSpecifications( specLocation );
				if( l != null && l.size() == 1 ) {
					YSpecification spec = (YSpecification) l.get( 0 );
					spec.setID( specURI.toString() );
					cache( specLocation, spec );
				}
				else {
					loadedObjects.put( specLocation, null );
				}
			}
			catch( Exception e ) {
				LOG.error( "error retrieving file " + specURI.toASCIIString(), e );
				loadedObjects.put( specLocation, null );
			}
			return loadedObjects.get( specLocation );
		}
		
		public boolean delete( YSpecification t ) {
			try {
				boolean b = new File( new URI( t.getID() ) ).delete();
				uncache( t );
				return b;
			}
			catch( Exception e ) {
				LOG.error( "Error deleting specification from file system!", e );
			}
			return false;
		}

		public List getChildren( Object object ) {
			LOG.debug( "getting file children of " + object );
			List retval = new ArrayList();
			if( object instanceof DatasourceFolder ) {
				String filename = ( (DatasourceFolder) object ).getPath();
				File f = null;
				try {
					f = new File( new URI( filename ) );
				}
				catch( URISyntaxException e ) {
					LOG.error( "bad file name in file::getChildren", e );
				}
				if( f != null ) {
					File[] files = null;
					files = f.listFiles();
					if( files != null ) {
						for( File aFile : files ) {
							if( !aFile.isHidden() ) {
								File file = new File( aFile.toURI() );
								if( file.isFile() && file.getName().toLowerCase().endsWith( ".xml" ) ) {
									YSpecification spec = retrieve( null, file.getAbsolutePath() );
									if( spec != null )
										retval.add( spec );
									else
										retval.add( new DatasourceFolder( file, (DatasourceFolder) object ) );
								}
								else
									retval.add( new DatasourceFolder( file, (DatasourceFolder) object ) );
							}
						}
					}
				}
			}
			return retval;
		}
	}
}
