/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool.model;

import java.io.Serializable;
import java.sql.Statement;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;

/**
 * 
 * @author Lachlan Aldred
 * Date: 23/09/2005
 * Time: 14:46:43
 */
@Entity
@Table(name="hresperformsrole")
public class HumanResourceRole implements Serializable {
    private Resource humanResource;
    private Role role;

    private Long _dbid;
    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    @Column(name="roleid")
    public Long getDbID() {
    	return _dbid;
    }

    public void setDbID(Long id) {
    	_dbid = id;
    }
    
    public HumanResourceRole() {
    }

    /*
     * Property inserted for backward compatability to old
     * yawl editors
     * */
    /*
    private String hresid = null;
    
    @Basic
    @Column(name="hresid")
    public String getHresid() {
		return hresid;
	}

	public void setHresid(String hresid) {
		this.hresid = hresid;
	}
*/
	@ManyToOne
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="hresid")
    public Resource getHumanResource() {
        return humanResource;
    }

    public void setHumanResource(Resource humanResource) {
        if(humanResource instanceof HumanResource) {
            this.humanResource = humanResource;
            
        } else {
            throw new IllegalArgumentException("<resource> is not instance of HumanResource");
        }
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="rolename")
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HumanResourceRole)) return false;

        final HumanResourceRole humanResourceRole = (HumanResourceRole) o;

        if (!humanResource.equals(humanResourceRole.humanResource)) return false;
        if (!role.equals(humanResourceRole.role)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = humanResource.hashCode();
        result = 29 * result + role.hashCode();
        return result;
    }


    /**
     * This hack ensures that referential integrity is maintained.  It dropps
     * the table generated by hibernate and replaces it with a better one.
     * @param session
     */
    public static void addIntegrityEnforcements(Session session) throws HibernateException {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Statement st = session.connection().createStatement();
            st.executeUpdate(
                "DROP TABLE hresperformsrole;" +
                "CREATE TABLE hresperformsrole" +
                "(" +
                  "hresid varchar(255) NOT NULL," +
                  "rolename varchar(255) NOT NULL," +
                  "CONSTRAINT hresperformsrole_pkey PRIMARY KEY (hresid, rolename)," +
                  "CONSTRAINT ResourceFK FOREIGN KEY (hresid) REFERENCES resserposid (id) ON UPDATE CASCADE ON DELETE CASCADE," +
                  "CONSTRAINT RoleFK FOREIGN KEY (rolename) REFERENCES role (rolename) ON UPDATE CASCADE ON DELETE CASCADE" +
                ");"
            );
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }
}