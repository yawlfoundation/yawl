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
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jdom.JDOMException;

import com.nexusbpm.editor.tree.DatasourceRoot;
import command.EditorCommand;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.unmarshal.YMarshal;



public class SpecificationFileDAO implements SpecificationDAO{

	public File root;
	
	public boolean delete(YSpecification t) {
		new File(t.getID()).delete();
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
            	System.out.println(o.toString());
            	System.out.println(">>" + new File(o.toString()).toURI().toASCIIString());
            	List l = null;
				l = YMarshal.unmarshalSpecifications( new File(o.toString()).toURI().toASCIIString());
				if (l != null && l.size() == 1) {
					retval = (YSpecification) l.get(0);
					retval.setID(new File(o.toString()).toURI().toString());
				}
            } catch (YSyntaxException e) {
				e.printStackTrace();
			} catch (YSchemaBuildingException e) {
				e.printStackTrace();
			} catch (JDOMException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
	            return retval;
	}

	public int save(YSpecification m) {
        File f = null;
		try {
			f = new File(new URI(m.getID()));
		} catch (Exception e1) {
			System.err.println(m.getID());
			e1.printStackTrace();
		}
        try {
        	FileWriter os = new FileWriter(f);
        	os.write(YMarshal.marshal(m));
        	os.flush();
        	os.close();
        } catch(Exception e) {e.printStackTrace();}
        return 0;
    }

    public Serializable getKey(YSpecification m) {
        return m.getID();
    }

	public List getChildren(Object filename) {
		System.out.println("getting children of " + filename);
		List retval = new ArrayList();
		if (filename instanceof String || filename instanceof DatasourceRoot) {
			filename = filename.toString();
			File f = null;
			try {
				f = new File(new URI((String) filename));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (f.isFile() && f.getName().endsWith(".xml")) {
				YSpecification spec = retrieve(f.getAbsolutePath());
				retval.add(spec);
			} else {
				File[] files = null;
				try {
					files = (new File(new URI((String) filename))).listFiles();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
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
