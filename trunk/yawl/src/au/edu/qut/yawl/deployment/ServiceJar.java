package au.edu.qut.yawl.deployment;

import java.net.URL;

public class ServiceJar {

	private long modified = 0;
	
	private URL filelocation = null;

	public ServiceJar(URL fileLocation, long modified) {
		super();
		this.modified = modified;
		this.filelocation = fileLocation;
	}

	public URL getFilelocation() {
		return filelocation;
	}

	public void setFilelocation(URL filelocation) {
		this.filelocation = filelocation;
	}

	public long getModified() {
		return modified;
	}

	public void setModified(long modified) {
		this.modified = modified;
	}
	
	
	
}
