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
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.unmarshal.YMarshal;

public class SpecificationFileDAO implements SpecificationDAO {

	private static final Log LOG = LogFactory.getLog(SpecificationFileDAO.class);

	public File root;
    
    /* cache the loaded specs so we're not constantly going back out to the file system. */
    private Map<String,YSpecification> loadedSpecs = new HashMap<String,YSpecification>();
    private Map<YSpecification,String> reverseMap = new HashMap<YSpecification,String>();
	
	public boolean delete(YSpecification t) {
		try {
			boolean b = new File(new URI(t.getID())).delete();
            uncache( t );
            return b;
		} catch (Exception e) {
		}
        return false;
	}

	public SpecificationFileDAO() {
		root = new File(".");
	}
	public SpecificationFileDAO(File root) {
		this.root = root;
	}
    
    private void cache( String key, YSpecification spec ) {
        loadedSpecs.put( key, spec );
        reverseMap.put( spec, key );
    }
    
    private void uncache( YSpecification spec ) {
        String key = reverseMap.get( spec );
        reverseMap.remove( spec );
        loadedSpecs.remove( key );
    }

	public YSpecification retrieve(Object o) {
            if (o == null) {
				return null;
			}
            String key = new File(o.toString()).toURI().toASCIIString();
            if( loadedSpecs.containsKey( key ) )
                return loadedSpecs.get( key );
            try {
            	String specLocation = key;
            	LOG.info("retrieving file spec " + specLocation);
            	List l = null;
				l = YMarshal.unmarshalSpecifications( specLocation );
				if (l != null && l.size() == 1) {
					YSpecification spec = (YSpecification) l.get(0);
					spec.setID(new File(o.toString()).toURI().toString());
                    cache( key, spec );
				}
                else {
                    loadedSpecs.put( key, null );
                }
            } catch (Exception e) {
				LOG.error("error retrieving file " + new File(o.toString()).toURI().toASCIIString(), e);
                loadedSpecs.put( key, null );
			}
            return loadedSpecs.get( key );
	}

	public int save(YSpecification m) {
		File f = null;
		try {
            if( ! m.getID().toLowerCase().endsWith(".xml") )
                m.setID( m.getID() + ".xml" );
	        LOG.info("saving " + m.getID());
	        f = new File(new URI(m.getID()));
            if( f.getParentFile() != null )
                f.getParentFile().mkdirs();
            m.setID( new URI(m.getID()).toASCIIString() );
        	FileWriter os = new FileWriter(f);
        	os.write(YMarshal.marshal(m));
        	os.flush();
        	os.close();
            String key = new File(m.getID()).toURI().toASCIIString();
            cache( key, m );
        } catch(Exception e) {
        	LOG.error("error saving " + m.getID() + " to file", e);
        }
        return 0;
    }

    public Serializable getKey(YSpecification m) {
        return m.getID();
    }

	public List getChildren(Object filename) {
		LOG.debug("getting file children of " + filename);
		List retval = new ArrayList();
        if( filename instanceof String || filename instanceof DatasourceRoot ) {
            filename = filename.toString();
        }
        else if( filename instanceof File ) {
            filename = ((File) filename).toURI().toString();
        }
		if( filename instanceof String ) {
			File f = null;
			try {
				f = new File(new URI((String) filename));
			} catch (URISyntaxException e) {
				LOG.error("bad file name in file::getChildren", e);
			}
			if (f.isFile() && ! f.isHidden() && f.getName().toLowerCase().endsWith(".xml")) {
				YSpecification spec = retrieve(f.getAbsolutePath());
                if( spec != null )
                    retval.add(spec);
			} else {
				File[] files = null;
				files = f.listFiles();
				if (files != null) {
					for (File aFile : files) {
                        if( !aFile.isHidden() ) {
//                            String file = aFile.toURI().toString();
                            File file = new File( aFile.toURI() );
//                            if (file.endsWith("/")) file = file.substring(0, file.length() - 1); 
                            retval.add(file);
                        }
					}
				}
			}
		}
		return retval;
	}
}
