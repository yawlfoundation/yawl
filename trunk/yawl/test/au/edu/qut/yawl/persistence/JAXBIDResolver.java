/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;

import com.sun.xml.bind.IDResolver;

public class JAXBIDResolver extends IDResolver
{  
	Map objects= new HashMap();
	  public Unmarshaller.Listener createListener() {
//    	  System.out.println("create listener");
	    return new Unmarshaller.Listener() {
			public void afterUnmarshal(Object target, Object parent) {
				super.afterUnmarshal(target, parent);
			}

		public void beforeUnmarshal(Object target, Object parent) {
			super.afterUnmarshal(target, parent);
      }
	  };
	  }
	  
	  public void endDocument() {
	  }

	  public void startDocument(ValidationEventHandler veh) {
	  }

	  public void bind(String id, Object obj) {
//    	  System.out.println("bind " + id + " to " + obj.toString() + " which is a " + obj.getClass().getName());
//		  if (obj instanceof YExternalNetElement)
		  objects.put(id, obj);
	  }

	  public Callable resolve(final String id, final Class targetType) {
//	    System.out.println("resolve " + id + ":" + targetType);
	    return new Callable() {
	      public Object call() throws Exception {
//	    	  System.out.println("resolvecall " + id + " to " + targetType + " returns " + objects.get(id));
//	    	  System.out.println("resolveset " + objects.toString());
	    	  return objects.get(id);
	      }
	      };
	  }
}	
