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
import java.util.List;

import org.jdom.JDOMException;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.unmarshal.YMarshal;



public class SpecificationFileDAO implements SpecificationDAO{

	public boolean delete(YSpecification t) {
		new File(t.getID()).delete();
		return true;
	}

	public YSpecification retrieve(Object o) {
		YSpecification retval = null;
            if (o == null) {
				return null;
			}
            try {
				List l = YMarshal.unmarshalSpecifications(o.toString());
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
        File f = new File(m.getID());
        try {
        	FileWriter os = new FileWriter(f);
        	os.write(YMarshal.marshal(m));
        	os.flush();
        	os.close();
        } catch(Exception e) {e.printStackTrace();}
        return 0;
    }

    public Object getKey(YSpecification m) {
        return m.getID();
    }
}
