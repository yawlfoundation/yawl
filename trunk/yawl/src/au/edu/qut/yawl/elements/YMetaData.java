/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.hibernate.annotations.CollectionOfElements;

import au.edu.qut.yawl.persistence.PersistableObject;

/**
 * Holds the Specification Metadata
 *
 * 
 * @author Lachlan Aldred
 * Date: 3/08/2005
 * Time: 18:47:47
 * 
 * 
 * @hibernate.class table="YMETA_DATA"
 */
@Entity
@Table(name="ymetadata")
@XmlRootElement(name="MetaDataType", namespace="http://www.citi.qut.edu.au/yawl")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MetaDataType", propOrder = {
    "title",
    "creators",
    "subjects",
    "description",
    "contributors",
    "coverage",
    "validFrom",
    "validUntil",
    "created",
    "version",
    "status"
})
public class YMetaData implements PersistableObject {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
	@XmlElement(name="title", namespace = "http://www.citi.qut.edu.au/yawl")
    private String title;
    @XmlElement(name="creator", namespace = "http://www.citi.qut.edu.au/yawl")
    private Set<String> creators = new HashSet<String>();
    @XmlElement(name="subject", namespace = "http://www.citi.qut.edu.au/yawl")
    private Set<String> subjects = new HashSet<String>();
	@XmlElement(name="description", namespace = "http://www.citi.qut.edu.au/yawl")
    private String description;
    @XmlElement(name="contributor", namespace = "http://www.citi.qut.edu.au/yawl")
    private Set<String> contributors = new HashSet<String>();
    private String coverage;
    private Date validFrom;
    private Date validUntil;
    private Date created;
    @XmlElement(namespace = "http://www.citi.qut.edu.au/yawl")
    private String version;
    private String status;
    public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public YMetaData() {
    }
    @XmlTransient
    private Long id = null;
    /**
     * Method inserted for hibernate use only
     * @hibernate.id column="YMETADATA_ID"
     */
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    protected Long getID() {
		return this.id;
	}

    /**
     * Method inserted for hibernate use only
     */
    protected void setID( Long id ) {
		this.id = id;
	}

	/**
     * 
     * @return
     * @hibernate.property column="TITLE"
     */
    @Column(name="title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

	/**
     * 
     * @return
     * @hibernate.property column="DESCRIPTION"
     */
    @Column(name="description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

	/**
     * 
     * @return
     * @hibernate.property column="COVERAGE"
     */
    @Column(name="coverage")
    public String getCoverage() {
        return coverage;
    }

    public void setCoverage(String coverage) {
        this.coverage = coverage;
    }

	/**
     * 
     * @return
     * @hibernate.property column="VALID_FROM"
     */
    @Column(name="valid_from")
    public Date getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

	/**
     * 
     * @return
     * @hibernate.property column="VALID_UNTIL"
     */
    @Column(name="valid_until")
    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }

	/**
     * 
     * @return
     * @hibernate.property column="CREATED"
     */
    @Column(name="created")
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

	/**
     * 
     * @return
     * @hibernate.property column="YVERSION"
     */
    @Column(name="version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

	/**
     * 
     * @return
     * @hibernate.property column="STATUS"
     */
    @Column(name="status")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

	/**
     * 
     * @return
     * @hibernate.property 
     * TODO Set<String>
     */
    @CollectionOfElements
    public Set<String> getCreators() {
        return creators;
    }

	/**
     * 
     * @return
     * @hibernate.property 
     * TODO Set<String>
     */
    @CollectionOfElements
    public Set<String> getSubjects() {
        return subjects;
    }
    
	/**
     * 
     * @return
     * @hibernate.property 
     * TODO Set<String>
     */
    @CollectionOfElements
    public Set<String> getContributors() {
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
        if (version != null) {
            mds.append("<version>" + version + "</version>");
        }
        if (status != null) {
            mds.append("<status>" + status + "</status>");
        }

        mds.append("</metaData>");
        return mds.toString();
    }
    @XmlTransient
    private YSpecification specification;
    
    @OneToOne(mappedBy = "metaData")
    private YSpecification getSpecification() {
		return specification;
	}

	
    private void setSpecification( YSpecification specification ) {
		this.specification = specification;
	}
    
}
