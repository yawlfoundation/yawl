/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;

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

public class YMetaData implements Cloneable, Serializable {
	public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
    /**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
    private Set<String> contributors = new HashSet<String>();
    private String coverage;
    private Date created;
    private Set<String> creators = new HashSet<String>();
    private String description;
    private Long id;
    private YSpecification specification;
    private String status;
    private Set<String> subjects = new HashSet<String>();
    private String title;
    private Date validFrom;
    private Date validUntil;
    private String version;

    public YMetaData() {
    }

    @Override
	public Object clone() throws CloneNotSupportedException {
		YMetaData newData = (YMetaData) super.clone();
        
        newData.contributors = new HashSet<String>( contributors );
        newData.creators = new HashSet<String>( creators );
        newData.subjects = new HashSet<String>( subjects );
        
        if( created != null ) {
            newData.created = (Date) created.clone();
        }
        newData.specification = null;
        if( validFrom != null ) {
            newData.validFrom = (Date) validFrom.clone();
        }
        if( validUntil != null ) {
            newData.validUntil = (Date) validUntil.clone();
        }
        
		return newData;
	}

	/**
     * 
     * @return
     * @hibernate.property 
     */
    @CollectionOfElements(fetch=FetchType.EAGER)
    public Set<String> getContributors() {
        return contributors;
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

	/**
     * 
     * @return
     * @hibernate.property column="CREATED"
     */
    @Column(name="created")
    public Date getCreated() {
        return created;
    }

    /**
     * 
     * @return
     * @hibernate.property 
     */
    @CollectionOfElements(fetch=FetchType.EAGER)
    public Set<String> getCreators() {
        return creators;
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

    /**
     * Method inserted for hibernate use only
     * @hibernate.id column="YMETADATA_ID"
     */
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    protected Long getID() {
		return this.id;
	}

	@OneToOne(mappedBy = "metaData")
    private YSpecification getSpecification() {
		return specification;
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
     * @hibernate.property 
     */
    @CollectionOfElements(fetch=FetchType.EAGER)
    public Set<String> getSubjects() {
        return subjects;
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

	/**
     * 
     * @return
     * @hibernate.property column="VALID_FROM"
     */
    @Column(name="valid_from")
    public Date getValidFrom() {
        return validFrom;
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

    public void setContributor(String contributor) {
        this.contributors.add(contributor);
    }

	public void setContributors(Set contributors) {
        this.contributors = contributors;
    }

	public void setCoverage(String coverage) {
        this.coverage = coverage;
    }
    
	public void setCreated(Date created) {
        this.created = created;
    }

    public void setCreator(String creator) {
        this.creators.add(creator);
    }

    public void setCreators(Set creators) {
        this.creators = creators;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Method inserted for hibernate use only
     */
    protected void setID( Long id ) {
		this.id = id;
	}

    private void setSpecification( YSpecification specification ) {
		this.specification = specification;
	}

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSubject(String subject) {
        this.subjects.add(subject);
    }
    public void setSubjects(Set<String> subjects) {
        this.subjects = subjects;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }

	
    public void setValidFrom(Date validFrom) {
        this.validFrom = validFrom;
    }

	public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }
	public String toXML() {
        StringBuffer mds = new StringBuffer();
        mds.append("<metaData>");
        if (this.title != null) {
            mds.append("<title>" + title + "</title>");
        }
        for (Object element : creators) {
            mds.append("<creator>" + element + "</creator>");
        }
        for (Object element : subjects) {
            mds.append("<subject>" + element + "</subject>");
        }
        if (description != null) {
            mds.append("<description>" + description + "</description>");
        }
        for (Object element : contributors) {
            mds.append("<contributor>" + element + "</contributor>");
        }
        if (coverage != null) {
            mds.append("<coverage>" + coverage + "</coverage>");
        }
        if (validFrom != null) {
            mds.append("<validFrom>" + YMetaData.dateFormat.format(validFrom) + "</validFrom>");
        }
        if (validUntil != null) {
            mds.append("<validUntil>" + YMetaData.dateFormat.format(validUntil) + "</validUntil>");
        }
        if (created != null) {
            mds.append("<created>" + YMetaData.dateFormat.format(created) + "</created>");
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

}
