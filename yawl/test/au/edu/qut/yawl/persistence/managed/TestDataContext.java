package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.List;
import java.util.Set;

import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import junit.framework.TestCase;

public class TestDataContext extends TestCase implements VetoableChangeListener{

	private PropertyChangeEvent lastEvent;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
		lastEvent = evt;
	}

	public void testFileDataContext() {
		DAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.FILE).getSpecificationModelDAO();
		DataContext dc = new DataContext(memdao);
		DataProxy<YSpecification> dp;
		dp = dc.getDataProxy("./bin/xyz.xml", this); // gets a "virtual"
		DataProxy root = dp;
		Set l = dc.getChildren(root);
		System.out.println(l.size());
		for (DataProxy o: (Set<DataProxy>) l) {
			System.out.println(o.getData().toString() + ":" + o.getData().getClass().getName());
		}
	}
	
	
	public void testMemDataContext() {
		DAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
		DataContext dc = new DataContext(memdao);
		DataProxy<YSpecification> dp;
		dp = dc.getDataProxy("/home/msandoz", this);
		DataProxy root = dp;
		
		getSpecProxy(dc, "/home/aTest", "aTest");
		getSpecProxy(dc, "/home/msandoz/aTest", "aTest");
		dp = getSpecProxy(dc, "/home/msandoz/templates/bTest", "bTest");
		YSpecification spec = dp.getData();
		DataProxy dp2 = dc.getDataProxy(spec, this);

		System.out.println(dc.getChildren(root).size());
		
		assertEquals(dp, dp2);
		Object o = dc.getKeyFor(dp);
		assertNotNull(o);
		assertEquals(o, spec.getID());
		lastEvent = null;
		try {
			dp.setAttribute("name", "aNewName");
		}
		catch(Exception e) {fail("should have been able to set attribute");}
		assertEquals(lastEvent.getSource(), spec);
		assertEquals(lastEvent.getPropertyName(), "name");
		assertEquals(lastEvent.getOldValue(), "bTest");
		assertEquals(lastEvent.getNewValue(), "aNewName");
		dc.put(dp);
		dc.remove(dp);
		try {
			dc.getKeyFor(dp2);
			fail("Should have thrown an exception");
		} catch (NullPointerException npe) {}
	}

	public DataProxy<YSpecification> getSpecProxy(DataContext dc, String uri, String name) {
		YSpecification spec = new YSpecification();
		spec.setName(name);
		spec.setID(uri);
		DataProxy<YSpecification> dp = dc.getDataProxy(spec, this);
		dc.put(dp);
		return dp;
	}
	
	public void testURL() {
		try {
			URLStreamHandlerFactory f = new MyURLStreamHandlerFactory("local");
			URL.setURLStreamHandlerFactory(f);

			File f2 = new File("C:\\temp\\XYZ.xml");
			URI fileUri = f2.toURI();
			URI memUri = new URI("local","memory","/home/msandoz/", null);
			String path = fileUri.getPath().substring(fileUri.getPath().lastIndexOf("/") + 1);
			URI destUri = memUri.resolve(path);

			memUri.toURL();//fails without a handler....
			
			System.out.println("source: " + fileUri.toString());
			System.out.println("dest:   " + memUri.toString());
			System.out.println("result: " + destUri.toString());
			System.out.println("path:   " + destUri.getPath());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

	class MyURLStreamHandlerFactory implements URLStreamHandlerFactory {
		public MyURLStreamHandlerFactory(String protocol) {super();this.protocol = protocol;}
		private String protocol;
		public URLStreamHandler createURLStreamHandler(String protocol) {
			if (protocol.equals(this.protocol)) {
				return new MyURLStreamHandler();
			}
			else return null;
		}
	}

	class MyURLStreamHandler extends URLStreamHandler{
		public URLConnection openConnection(URL url) {
			URLConnection uc = null;//new URLConnection(url) {;
			return uc;
		}
	}
