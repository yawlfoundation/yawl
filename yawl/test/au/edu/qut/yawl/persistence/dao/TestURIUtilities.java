package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLDecoder;

import junit.framework.TestCase;

public class TestURIUtilities extends TestCase {

	private static URI uri1;
	private static URI uri2;
	private static URI uri3;
	private static URI uri4;
	private static URI uri5;
	private static URI uri6;

	private static final String uriSpec1 = "exampleSpecs/le testing/Spec du François.xml";
	private static final String uriSpec2 = "/home/sandozm/templates/le testing/Spec du François.xml";
	private static final String uriSpec3 = "http://www.junit.org:80/something";
	private static final String uriSpec4 = "mailto:java-net@java.sun.com";
	private static final String uriSpec6 = "exampleSpecs/le testing/ciel du monde";
	
	
	
	private URI joinURIs(URI parent, URI child) {
		URI retval = null;
		String text = child.getRawPath();
		int index = text.lastIndexOf("/") + 1;
		try {
			retval = new URI(parent.getScheme(), parent.getAuthority(), parent.getPath() + "/" + URLDecoder.decode(text.substring(index), "UTF-8"), child.getQuery(), child.getFragment());
			retval = retval.normalize();
		} catch (Exception e) {e.printStackTrace();}
		return retval;
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		uri1 = new File(uriSpec1).toURI();
		uri2 = new URI("virtual", "memory", uriSpec2, null,null);
		uri6 = new File(uriSpec6).toURI();
		uri5 = joinURIs(uri6, uri2);
		System.out.println(">>" + uri5);
		uri3 = new URI(uriSpec3);
		uri4 = new URI(uriSpec4);
		File f = new File(uri5);
		URI uri7 = f.toURI();
		System.out.println(".." + new URI(uri7.toString()));
		f.delete();
		f.getParentFile().mkdirs();
		f.createNewFile();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		File f = new File(uri5);
		f.delete();
	}
	
	public void testCharacters() {
		try {
			System.out.println(uri1.toURL().toExternalForm());
			System.out.println(uri1.toURL().toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		File f = new File(uri5);
		System.out.println(f.exists());
	}
}