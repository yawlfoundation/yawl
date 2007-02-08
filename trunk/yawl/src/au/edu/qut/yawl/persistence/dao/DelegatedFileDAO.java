/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSyntaxException;
import au.edu.qut.yawl.persistence.DatasourceFolder;
import au.edu.qut.yawl.persistence.dao.restrictions.Restriction;
import au.edu.qut.yawl.unmarshal.YMarshal;

public class DelegatedFileDAO extends AbstractDelegatedDAO {
	private static final Log LOG = LogFactory.getLog(DelegatedFileDAO.class);

	public DelegatedFileDAO() {
		addType(YSpecification.class, new SpecificationFileDAO());
	}

	private abstract class AbstractFileDAO<Type> implements DAO<Type> {
		/* cache the loaded objects so we're not constantly going back out to the file system. */
		protected Map<Object, Type> loadedObjects = new HashMap<Object, Type>();

		protected Map<Type, Object> reverseMap = new HashMap<Type, Object>();

		protected void cache(Object key, Type object) {
			loadedObjects.put(key, object);
			reverseMap.put(object, key);
		}

		protected void uncache(Type object) {
			Object key = reverseMap.get(object);
			reverseMap.remove(object);
			loadedObjects.remove(key);
		}

		public List<Type> retrieveByRestriction(Class type,
				Restriction restriction) {
			// restrictions don't make sense in the context of a hierarchical filesystem the
			// same way they do for a database or memory
			throw new UnsupportedOperationException(
					"The file system DAO does not support retrieval by restriction!");
		}
	}

	private class SpecificationFileDAO extends AbstractFileDAO<YSpecification> {
		public Object getKey(YSpecification object) {
			return PersistenceUtilities.getSpecificationKey(object);
		}

		public void save(YSpecification spec) {
			File f = null;
			try {
				if (!spec.getID().toLowerCase().endsWith(".xml"))
					spec.setID(spec.getID() + ".xml");
				System.out.println("saving " + spec.getID() + " as " + "file://" + spec.getID());
				f = new File(new URI(null, null, spec.getID(), null));
				if (f.getParentFile() != null)
					f.getParentFile().mkdirs();
				spec.setID(new URI(spec.getID()).toASCIIString());
				FileWriter os = new FileWriter(f);
				os.write(YMarshal.marshal(spec));
				os.flush();
				os.close();
				String key = new File(spec.getID()).toURI().toASCIIString();
				cache(key, spec);
			} catch (Exception e) {
				LOG.error("error saving " + spec.getID() + " to file", e);
			}
		}

		public YSpecification retrieve(Class type, Object key) {
			if (key == null) {
				return null;
			}
			URI specURI = new File(key.toString()).toURI();
			String specLocation = key.toString();
			YSpecification spec = null;
			try {
				String xml = getFileContents(new File(specURI.getPath()));
				spec = retrieveFromContents(specLocation, xml);
			} catch (FileNotFoundException e) {
				LOG.info("error retrieving file spec " + specLocation, e);
			}
			return spec;
		}

		private YSpecification retrieveFromContents(String specLocation, String xml) {
			YSpecification spec = null;
			if (loadedObjects.containsKey(specLocation)) {
				return loadedObjects.get(specLocation);
			}
			try {
				LOG.info("retrieving file spec " + specLocation);
				List l = YMarshal.unmarshalSpecifications(xml, specLocation);
				if (l != null && l.size() == 1) {
					spec = (YSpecification) l.get(0);
					spec.setID(specLocation);
				} 
			} catch (Exception e) {
				LOG.error("error retrieving file " + specLocation, e);
			}
			if (spec != null) {
				cache(specLocation, spec);
			} else {
				loadedObjects.put(specLocation, null);
			}
			return loadedObjects.get(specLocation);		
		}
		
		public void delete(YSpecification t) throws YPersistenceException {
			try {
				new File(new URI(t.getID())).delete();
				uncache(t);
			} catch (Exception e) {
				throw new YPersistenceException(
						"Error deleting specification from file system!", e);
			}
		}

		public List getChildren(Object object) {
			LOG.debug("getting file children of " + object);
			List retval = new ArrayList();
			if (object instanceof DatasourceFolder) {
				String filename = ((DatasourceFolder) object).getPath();
				File f = null;
				try {
					f = new File(new URI(filename));
				} catch (URISyntaxException e) {
					LOG.error("bad file name in file::getChildren", e);
				}
				if (f != null) {
					File[] files = null;
					files = f.listFiles(new YawlFileFilter());
					if (files != null) {
						for (File aFile : files) {
							File file = new File(aFile.toURI());
								YSpecification spec = retrieve(YSpecification.class, aFile.toURI().getPath());
								if (spec != null) {
									retval.add(spec);
								}
								else {
									retval.add(new DatasourceFolder(file,(DatasourceFolder) object));
								}
						}
					}
				}
			}
			return retval;
		}

		public String getFileContents(File pathname)
				throws FileNotFoundException {
			Scanner scanner = new Scanner(pathname).useDelimiter("\\A");
			String text = null;
			try {
				if (scanner.hasNext()) {
					text = scanner.next();
				}
			} catch (java.util.NoSuchElementException e) {
				System.err.println(pathname.getAbsolutePath());
				e.printStackTrace();
			}
			scanner.close();
			return text;
		}

		class YawlFileFilter implements FileFilter {
			public boolean accept(File pathname) {
				boolean retval = false;
				if (!pathname.isHidden()) {
					if (pathname.getName().toLowerCase().endsWith(".xml")) {
						try {
							String text = getFileContents(pathname);
							if ((text != null) && text.contains("specificationSet")) {
								retrieveFromContents(pathname.toURI().toASCIIString(), text);
								retval = true;
							}
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (pathname.isDirectory()) {
						retval = true;
					}
				}
				return retval;
			}

		}
	}
}
