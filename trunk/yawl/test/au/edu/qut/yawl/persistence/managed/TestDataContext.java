package au.edu.qut.yawl.persistence.managed;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import junit.framework.TestCase;
import au.edu.qut.yawl.elements.YSpecification;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.DAOFactory;
import au.edu.qut.yawl.persistence.dao.DatasourceFolder;
import au.edu.qut.yawl.persistence.dao.DatasourceRoot;
import au.edu.qut.yawl.persistence.dao.DAOFactory.PersistenceType;
import au.edu.qut.yawl.exceptions.YPersistenceException;

public class TestDataContext extends TestCase implements DataProxyStateChangeListener {

	private PropertyChangeEvent lastEvent;
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

    public void proxyAttached( DataProxy proxy, Object data, DataProxy parent ) {
    }

    public void proxyAttaching( DataProxy proxy, Object data, DataProxy parent ) {
    }

    public void proxyDetached( DataProxy proxy, Object data, DataProxy parent ) {
    }

    public void proxyDetaching( DataProxy proxy, Object data, DataProxy parent ) {
    }

    public void propertyChange( PropertyChangeEvent evt ) {
        lastEvent = evt;
    }

	public void testMemDataContext() {
//		DAO memdao = DAOFactory.getDAOFactory(DAOFactory.Type.MEMORY).getSpecificationModelDAO();
		DAO memdao = DAOFactory.getDAO( PersistenceType.MEMORY );
		DataContext context = new DataContext(memdao);
        DataProxy<YSpecification> proxy = null;
        
        DatasourceRoot root = new DatasourceRoot("/home");
        DataProxy rootProxy = context.createProxy(root, this);
        context.attachProxy(rootProxy, root, null);
        
        DatasourceFolder home = new DatasourceFolder("/home/msandoz", root);
		DataProxy homeProxy = context.createProxy(home,this);
        context.attachProxy(homeProxy, home, rootProxy);
		
		try  {
			createSpecAndProxy(context, "/home/aTest", "aTest", rootProxy);
			createSpecAndProxy(context, "/home/msandoz/aTest", "aTest", homeProxy);
		} catch (YPersistenceException e) {
			fail("PersistenceException should not have been thrown");
		}
        
        DatasourceFolder templates = new DatasourceFolder("/home/msandoz/templates",home);
        DataProxy templatesProxy = context.createProxy(templates, this);
        context.attachProxy(templatesProxy, templates, homeProxy);
        try {
        	proxy = createSpecAndProxy(context, "/home/msandoz/templates/bTest", "bTest", templatesProxy);
		} catch (YPersistenceException e) {
			fail("PersistenceException should not have been thrown");
		}
		YSpecification spec = proxy.getData();
		DataProxy proxy2 = context.getDataProxy(spec);
	
		assertEquals(proxy, proxy2);
		Object o = context.getKeyFor(proxy);
		assertNotNull(o);
		assertEquals(o, spec.getID());
		lastEvent = null;
		try {
			proxy.setAttribute("name", "aNewName");
		}
		catch(Exception e) {fail("should have been able to set attribute");}
		assertEquals(lastEvent.getSource(), context.getDataProxy(spec));
		assertEquals(lastEvent.getPropertyName(), "name");
		assertEquals(lastEvent.getOldValue(), "bTest");
		assertEquals(lastEvent.getNewValue(), "aNewName");
		try  {
			context.save(proxy);
		} catch (YPersistenceException e) {
			fail("PersistenceException should not have been thrown");
		}
		context.delete(proxy);
		try {
			context.getKeyFor(proxy2);
			fail("Should have thrown an exception");
		} catch (NullPointerException npe) {}
	}

	public DataProxy<YSpecification> createSpecAndProxy(
            DataContext dc, String uri, String name, DataProxy parent) throws YPersistenceException {
		YSpecification spec = new YSpecification(uri);
		spec.setName(name);
		DataProxy<YSpecification> dp = dc.createProxy(spec,this);
        dc.attachProxy(dp, spec, parent);
		dc.save(dp);
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
