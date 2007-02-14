package au.edu.qut.yawl.persistence.dao;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

import junit.framework.TestCase;

import org.apache.commons.codec.EncoderException;

public class UriAdapterTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testFullUri() throws URISyntaxException  {
		String testUri = "user:pwd@www.xyz.com:80/abc/def/ghi.jkl?mno=pqr&stu=vwx";
		UriAdapter adapter = new UriAdapter("http", testUri);
		assertEquals("jkl", adapter.getExtension());
		assertEquals("ghi", adapter.getFilename());
		assertEquals("/abc/def", adapter.getPath());
		assertEquals("http", adapter.getScheme());
	}

	public void testSomething() {
		char foreign = 'ç';
		char domestic = ' ';
		System.out.println(Character.isDefined(foreign));
		System.out.println(Character.isISOControl(foreign));
		System.out.println(Character.isUnicodeIdentifierPart(foreign));
		System.out.println(Character.isWhitespace(foreign));

		System.out.println(Character.isDefined(domestic));
		System.out.println(Character.isISOControl(domestic));
		System.out.println(Character.isUnicodeIdentifierPart(domestic));
		System.out.println(Character.isWhitespace(domestic));
	}
	
	
	public void testStrangeUri() throws URISyntaxException, UnsupportedEncodingException, EncoderException {
		String testUri = "//www.xyz.com:80/Spec François.xml";
		UriAdapter adapter = new UriAdapter("http", testUri);
		System.out.println(">>" + testUri);
		System.out.println(">>" + adapter.toString());
		assertEquals("xml", adapter.getExtension());
		assertEquals("Spec François", adapter.getFilename());
		assertEquals(null, adapter.getPath());
		Character c;
	}
	
	
	public void testFileUri() throws URISyntaxException  {
		String testUri = "D:/path/to/resource.xml";
		UriAdapter adapter = new UriAdapter("file", testUri);
		assertEquals("xml", adapter.getExtension());
		assertEquals("resource", adapter.getFilename());
		assertEquals("/path/to", adapter.getPath());
		assertEquals("file", adapter.getScheme());
	}

	public void testRelativeUri1() throws URISyntaxException  {
		String testUri = "path/to/resource.xml";
		UriAdapter adapter = new UriAdapter(null, testUri);
		assertEquals("xml", adapter.getExtension());
		assertEquals("resource", adapter.getFilename());
		assertEquals("path/to", adapter.getPath());
		assertEquals(null, adapter.getScheme());
	}
	
	public void testRelativeUri2() throws URISyntaxException  {
		String testUri = "/path/to/resource.xml";
		UriAdapter adapter = new UriAdapter(null, testUri);
		assertEquals("xml", adapter.getExtension());
		assertEquals("resource", adapter.getFilename());
		assertEquals("/path/to", adapter.getPath());
		assertEquals(null, adapter.getScheme());
	}
	
	public void testMoveRelative1() throws URISyntaxException {
		String testUri = "abc/def/ghi.jkl";
		String after = "def/ghi/ghi.jkl";
		UriAdapter adapter = new UriAdapter(null, testUri);
		adapter.setPath("def/ghi");
		assertEquals(after, adapter.getUri());
	}

	public void testMoveRelative2() throws URISyntaxException {
		String testUri = "/abc/def/ghi.jkl";
		String after = "/def/ghi/ghi.jkl";
		UriAdapter adapter = new UriAdapter(null, testUri);
		adapter.setPath("/def/ghi");
		assertEquals(after, adapter.getUri());
	}

	public void testMove() throws URISyntaxException {
		String testUri = "http://user:pwd@www.xyz.com:80/abc/def/ghi.jkl?mno=pqr&stu=vwx";
		String after = "http://user:pwd@www.xyz.com:80/def/ghi/ghi.jkl?mno=pqr&stu=vwx";
		UriAdapter adapter = new UriAdapter(null, testUri);
		adapter.setPath("/def/ghi");
		assertEquals(after, adapter.getUri());
	}

	public void testRename() throws URISyntaxException {
		String testUri = "http://user:pwd@www.xyz.com:80/abc/def/ghi.jkl?mno=pqr&stu=vwx";
		String after1 = "http://user:pwd@www.xyz.com:80/abc/def/ghi.com?mno=pqr&stu=vwx";
		String after2 = "http://user:pwd@www.xyz.com:80/abc/def/afterwards.com?mno=pqr&stu=vwx";
		UriAdapter adapter = new UriAdapter(null, testUri);
		adapter.setExtension("com");
		assertEquals(after1, adapter.getUri());
		adapter.setFilename("afterwards");
		assertEquals(after2, adapter.getUri());
	}

}
