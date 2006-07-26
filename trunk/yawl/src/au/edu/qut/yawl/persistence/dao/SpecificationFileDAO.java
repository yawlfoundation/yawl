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
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.unmarshal.YMarshal;

import com.nexusbpm.editor.tree.DatasourceRoot;



public class SpecificationFileDAO implements SpecificationDAO {

	private static final Log LOG = LogFactory.getLog(SpecificationFileDAO.class);

	public File root;
	
	public boolean delete(YSpecification t) {
		try {
			new File(new URI(t.getID())).delete();
		} catch (URISyntaxException e) {
			return false;
		}
		return true;
	}

	public SpecificationFileDAO() {
		root = new File(".");
	}
	public SpecificationFileDAO(File root) {
		this.root = root;
	}

	public YSpecification retrieve(Object o) {
		YSpecification retval = null;
            if (o == null) {
				return null;
			}
            try {
            	String specLocation = new File((o.toString())).toURI().toASCIIString();
            	LOG.info("retrieving file spec " + specLocation);
            	List l = null;
				l = YMarshal.unmarshalSpecifications( specLocation );
				if (l != null && l.size() == 1) {
					retval = (YSpecification) l.get(0);
					retval.setID(new File(o.toString()).toURI().toString());
				}
            } catch (Exception e) {
				LOG.error("error retrieving file " + new File(o.toString()).toURI().toASCIIString(), e);
			}
            return retval;
	}

	public int save(YSpecification m) {
		File f = null;
		try {
	        LOG.info("saving " + m.getID());
	        f = new File(new URI(m.getID()));
		} catch (Exception e1) {
			LOG.error("error saving " + m.getID() + " to file", e1);
		}
        try {
        	FileWriter os = new FileWriter(f);
        	os.write(YMarshal.marshal(m));
        	os.flush();
        	os.close();
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
		if (filename instanceof String || filename instanceof DatasourceRoot) {
			filename = filename.toString();
			File f = null;
			try {
				f = new File(new URI((String) filename));
			} catch (URISyntaxException e) {
				LOG.error("bad file name in file::getChildren", e);
			}
			if (f.isFile() && f.getName().endsWith(".xml")) {
				YSpecification spec = retrieve(f.getAbsolutePath());
                if( spec != null )
                    retval.add(spec);
			} else {
				File[] files = null;
				files = f.listFiles();
				if (files != null) {
					for (File aFile : files) {
						String file = aFile.toURI().toString();
//						if (file.endsWith("/")) file = file.substring(0, file.length() - 1); 
						retval.add(file);
					}
				}
			}
		}
		return retval;
	}
}
