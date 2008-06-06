/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.unmarshal;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Holds the Specification Metadata
 *
 * 
 * @author Lachlan Aldred
 * Date: 3/08/2005
 * Time: 18:47:47
 * 
 */
public class YMetaData {
	//MLR 22/10/2007 (merge): this is the initial value of a spec's version
	static final double INITIAL_VERSION = 0.1;
    static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private String title;
    private Set creators = new HashSet();
    private Set subjects = new HashSet();
    private String description;
    private Set contributors = new HashSet();
    private String coverage;
    private Date validFrom;
    private Date validUntil;
    private Date created;
    private double version = INITIAL_VERSION;
    private String status;
	private Boolean persistent;

    public YMetaData() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set getCreators() {
        return creators;
    }

    public Set getSubjects() {
        return subjects;
    }

    public Set getContributors() {
        return contributors;
    }

    public void setCreators(Set creators) {
        this.creators = creators;
    }

    public void setSubjects(Set subjects) {
        this.subjects = subjects;
    }

    public void setContributors(Set contributors) {
        this.contributors = contributors;
    }

    public void setCreator(String creator) {
        this.creators.add(creator);
    }

    public void setSubject(String subject) {
        this.subjects.add(subject);
    }

    public void setContributor(String contributor) {
        this.contributors.add(contributor);
    }

    public boolean isPersistent()
    {
        return persistent.booleanValue();
    }

    public void setPersistent(boolean persistent)
    {
        this.persistent = Boolean.valueOf(persistent);
    }

    public String toXML() {
        StringBuffer mds = new StringBuffer();
        mds.append("<metaData>");
        if (this.title != null) {
            mds.append("<title>" + title + "</title>");
        }
        for (Iterator iterator = creators.iterator(); iterator.hasNext();) {
            mds.append("<creator>" + iterator.next() + "</creator>");
        }
        for (Iterator iterator = subjects.iterator(); iterator.hasNext();) {
            mds.append("<subject>" + iterator.next() + "</subject>");
        }
        if (description != null) {
            mds.append("<description>" + description + "</description>");
        }
        for (Iterator iterator = contributors.iterator(); iterator.hasNext();) {
            mds.append("<contributor>" + iterator.next() + "</contributor>");
        }
        if (coverage != null) {
            mds.append("<coverage>" + coverage + "</coverage>");
        }
        if (validFrom != null) {
            mds.append("<validFrom>" + dateFormat.format(validFrom) + "</validFrom>");
        }
        if (validUntil != null) {
            mds.append("<validUntil>" + dateFormat.format(validUntil) + "</validUntil>");
        }
        if (created != null) {
            mds.append("<created>" + dateFormat.format(created) + "</created>");
        }

        mds.append("<version>" + version + "</version>");

        if (status != null) {
            mds.append("<status>" + status + "</status>");
        }
        if (persistent != null)
        {
            mds.append("<persistent>" + persistent.toString() + "</persistent>");
        }

        mds.append("</metaData>");
        return mds.toString();
    }
}
