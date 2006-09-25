/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.events;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Lob;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class YDataEvent implements Serializable {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    private String eventid = null;

    private String port = null;

    private String value = null;

    private String io = null;

    private Long id;
    
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
	public Long getId() {
		return id;
	}

	public void setId( Long id ) {
		this.id = id;
	}

	public void setEventid(String eventid) {
        this.eventid = eventid;
    }

    @Basic
    public String getEventid() {
        return this.eventid;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Basic
    public String getPort() {
        return this.port;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Lob
    public String getValue() {
        return this.value;
    }


    public void setIo(String io) {
        this.io = io;
    }

    @Basic
    public String getIo() {
        return this.io;
    }

}