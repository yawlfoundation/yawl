/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.events;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@Entity
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(
    name="errortype",
    discriminatorType=DiscriminatorType.STRING
)
@DiscriminatorValue("basicerror")
public class YErrorEvent {

	private Long _id;
	
    @Id
    @Column(name="error_id")
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    public Long getId() {
		return _id;
	}
    
	public void setId( Long id ) {
		_id = id;
	}
	
	private String task = null;
	
	private String workitem = null;

	@Basic
	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		this.task = task;
	}

	@Basic
	public String getWorkitem() {
		return workitem;
	}

	public void setWorkitem(String workitem) {
		this.workitem = workitem;
	}

	public YErrorEvent(String task, String workitem) {
		super();
		this.task = task;
		this.workitem = workitem;
	}
	
	public String toXML() {
		
		return startXML() + endXML();
		
	}

	public String startXML() {
		
		return "<ErrorEvent> <Task> " + task +  " </Task> <WorkItem> " + workitem +  " </WorkItem>";
		
	}

	public String endXML() {
		
		return "</ErrorEvent>";
		
	}

}
