/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.unmarshal;

import org.yawlfoundation.yawl.elements.YSpecVersion;
import org.yawlfoundation.yawl.util.StringUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

  	static final String INITIAL_VERSION = "0.1";
    static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private String title;
    private List<String> creators = new ArrayList<String>();
    private List<String> subjects = new ArrayList<String>();
    private String description;
    private List<String> contributors = new ArrayList<String>();
    private String coverage;
    private Date validFrom;
    private Date validUntil;
    private Date created;
    private YSpecVersion version = new YSpecVersion(INITIAL_VERSION);
    private String status;
	  private boolean persistent;
    private String uniqueID = null;                            // null for pre-2.0 specs

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

    public YSpecVersion getVersion() {
        return version;
    }

    public void setVersion(YSpecVersion version) {
        this.version = version;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getCreators() {
        return creators;
    }

    public List<String> getSubjects() {
        return subjects;
    }

    public List<String> getContributors() {
        return contributors;
    }

    public void setCreators(List<String> creators) {
        this.creators = creators;
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    public void addCreator(String creator) {
        this.creators.add(creator);
    }

    public void addSubject(String subject) {
        this.subjects.add(subject);
    }

    public void addContributor(String contributor) {
        this.contributors.add(contributor);
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String toXML() {
        StringBuilder mds = new StringBuilder();
        mds.append("<metaData>");
        if (this.title != null) {
            mds.append(StringUtil.wrap(title, "title"));
        }
        for (String creator : creators) {
            mds.append(StringUtil.wrap(creator, "creator"));
        }
        for (String subject : subjects) {
            mds.append(StringUtil.wrap(subject, "subject"));
        }
        if (description != null) {
            mds.append(StringUtil.wrap(description, "description"));
        }
        for (String contributor : contributors) {
            mds.append(StringUtil.wrap(contributor, "contributor"));
        }
        if (coverage != null) {
            mds.append(StringUtil.wrap(coverage, "coverage"));
        }
        if (validFrom != null) {
            mds.append(StringUtil.wrap(dateFormat.format(validFrom), "validFrom"));
        }
        if (validUntil != null) {
            mds.append(StringUtil.wrap(dateFormat.format(validUntil), "validUntil"));
        }
        if (created != null) {
            mds.append(StringUtil.wrap(dateFormat.format(created), "created"));
        }

        mds.append(StringUtil.wrap(version.toString(), "version"));

        if (status != null) {
            mds.append(StringUtil.wrap(status, "status"));
        }

        mds.append(StringUtil.wrap(String.valueOf(persistent), "persistent"));

        if (uniqueID != null) {
            mds.append(StringUtil.wrap(uniqueID, "identifier"));
        }

        mds.append("</metaData>");
        return mds.toString();
    }
}
