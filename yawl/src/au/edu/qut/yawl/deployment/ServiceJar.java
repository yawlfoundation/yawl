/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

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
