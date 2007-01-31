package au.edu.qut.yawl.persistence.dao;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import au.edu.qut.yawl.util.UriEncoder;

public class UriAdapter {

	private String extension;
	private String filename;
	private String path;
	private String scheme;
	private String sspart;

	private String fragment;
	private String query;
	private String authority;
	
	
	public UriAdapter(String scheme, String ssp) throws URISyntaxException {
		setUri(scheme, ssp);
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getScheme() {
		return scheme;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public String toDisplay() {
		try {
			String testUri = URLDecoder.decode(getUri(), "UTF-8");
			return testUri;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
			try {
				String testUri = URLDecoder.decode(new URI(getUri()).toASCIIString(), "UTF-8");
				System.out.println("<<" + new URI(getUri()).toASCIIString());
				System.out.println("<<" + new URI(getUri()).toString());
				return testUri;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
	}
	
	public String getUri() throws URISyntaxException{
		String testPath = this.path == null ? "" : this.path;
		String testName = this.filename == null ? "" : "/" + this.filename;
		String testExt = this.extension == null ? "" : "." + this.extension;
		String test = testPath + testName + testExt;
		URI testUri = new URI(this.scheme, this.sspart, this.fragment);
		return testUri.toString();
	}

	public void setUri(String scheme, String sspart) throws URISyntaxException {
		URI temp = new URI(scheme, sspart, null);
		this.sspart = temp.getSchemeSpecificPart();
		this.scheme = temp.getScheme();
//		this.fragment = temp.getFragment();
//		this.authority = temp.getAuthority();
//		this.query = temp.getQuery();
		System.out.println(">1" + temp.getRawSchemeSpecificPart());
		System.out.println(">2" + temp.getSchemeSpecificPart());
		int extensionLocation = temp.getSchemeSpecificPart().lastIndexOf(".");
		if (extensionLocation > 0 && extensionLocation < temp.getSchemeSpecificPart().length() ) {
			extension = temp.getSchemeSpecificPart().substring(extensionLocation + 1);
		} else {
			extension = null;
		}
		int lastPathLocation = temp.getSchemeSpecificPart().lastIndexOf("/");
		if (lastPathLocation > 0 && lastPathLocation < temp.getPath().length()) {
			if (extension.length() == 0) {
				this.filename = temp.getSchemeSpecificPart().substring(lastPathLocation);
			}
			else {
				this.filename = temp.getSchemeSpecificPart().substring(lastPathLocation + 1, extensionLocation);
			}
			this.path = temp.getSchemeSpecificPart().substring(0, lastPathLocation);
		} else {
			path = null;
			this.filename = temp.getSchemeSpecificPart().substring(0, extensionLocation);
		}
		
		
	}

	public static class FileUriAdapter extends UriAdapter{
		public FileUriAdapter(File root, File file) throws URISyntaxException{
			super("", "");
		}
	}	
}

