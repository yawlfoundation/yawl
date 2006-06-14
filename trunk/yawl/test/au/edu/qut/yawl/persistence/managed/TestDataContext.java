package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
	
	public void testURI() {
		try {
			URLStreamHandlerFactory f = new MyURLStreamHandlerFactory("local");
			URL.setURLStreamHandlerFactory(f);

			URI fileUri = new URI(toURIString("C:\\temp\\XYZ is Great.xml", false));
			URI memUri = new URI("virtual","memory","/home/msandoz/", null);
			URI destUri = moveUri(fileUri, memUri);
			assertEquals(destUri.toString(), "virtual://memory/home/msandoz/XYZ%20is%20Great.xml");

			URI mem2Uri = new URI("virtual","memory","/home/msandoz/XYZ is Great.xml", null);
			URI file2Uri = new URI(toURIString("C:\\temp\\", true));
			URI dest2Uri = moveUri(mem2Uri, file2Uri);
			assertEquals(dest2Uri.toString(), "file:/C:/temp/XYZ%20is%20Great.xml");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static URI moveUri(URI source, URI dest) throws URISyntaxException  {
		URI retval = null;
		String path = source.getPath().substring(source.getPath().lastIndexOf("/") + 1);
		retval = new URI(dest.getScheme(), dest.getHost(), dest.getPath() + path, null);
		return retval;
	}
    private static String slashify(String path, boolean isDirectory) {
    	String p = path;
    	if (File.separatorChar != '/')
    	    p = p.replace(File.separatorChar, '/');
    	if (!p.startsWith("/"))
    	    p = "/" + p;
    	if (!p.endsWith("/") && isDirectory)
    	    p = p + "/";
    	return p;
        }
    public static String toURIString(String s, boolean isDirectory) {
    	try {
    	    String sp = slashify(s, isDirectory);
    	    if (sp.startsWith("//"))
    		sp = "//" + sp;
    	    return new URI("file", null, sp, null).toString();
    	} catch (URISyntaxException x) {
    	    throw new Error(x);		// Can't happen
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
