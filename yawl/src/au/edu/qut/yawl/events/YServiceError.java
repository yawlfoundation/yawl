/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.events;

import javax.persistence.Basic;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("serviceerror")
public class YServiceError extends YErrorEvent {

	private String uri = null;

	public YServiceError(String uri, String task, String workitem) {
		super(task, workitem);
		this.uri = uri;
		// TODO Auto-generated constructor stub
	}

	@Basic
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
	
	public String toXML() {
		return super.startXML() + "<URI> " + uri + " </URI> " + super.endXML();					
	}
	
	
}
