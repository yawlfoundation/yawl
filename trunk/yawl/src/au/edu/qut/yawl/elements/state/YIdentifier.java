/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */
package au.edu.qut.yawl.elements.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.events.YErrorEvent;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;

/**
 * 
 * This class has control over data structures that allow for
 * storing an identifer and manging a set of children.
 * @author Lachlan Aldred
 * 
 */
@Entity
public class YIdentifier implements Serializable {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    /*************************/
    /* INSERTED VARIABLES AND METHODS FOR PERSISTANCE* /
    /**************************/
    private String id = null;

    /**
     */
    @Id
    @Column(name="id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    /************************************************/

    private List<YIdentifier> _children = new Vector<YIdentifier>();
    private YIdentifier _parent;
//    private Long specID;
    private String specURI;
    private Integer specVersion;


    public YIdentifier() {
    }

    public YIdentifier(String idString) {
        id = idString;
    }
    
    public YIdentifier(String specURI, Integer specVersion) {
        this.specURI = specURI;
        this.specVersion = specVersion;
    }

    @OneToMany(mappedBy="parent",cascade={CascadeType.REMOVE})
    public List<YIdentifier> getChildren() {
        return _children;
    }
    
    protected void setChildren(List<YIdentifier> children) {
    	_children = children;
    }
    
    @Column(name="specURI")
    public String getSpecURI() {
        return specURI;
    }
    
    public void setSpecURI(String specURI) {
        this.specURI = specURI;
    }
    
    @Column(name="specVersion")
    public Integer getSpecVersion() {
        return specVersion;
    }
    
    public void setSpecVersion(Integer specVersion) {
        this.specVersion = specVersion;
    }
    
//    @Column(name="specID")
//    public Long getSpecID() {
//        return specID;
//    }
//    
//    public void setSpecID(Long specID) {
//        this.specID = specID;
//    }

    @Transient
    public Set getDescendants() {
        Set descendants = new HashSet();
        descendants.add(this);

        if (_children.size() > 0) {
            Iterator childIter = _children.iterator();
            while (childIter.hasNext()) {
                descendants.addAll(((YIdentifier) childIter.next())
                        .getDescendants());
            }
        }
        return descendants;
    }
    
    public static void saveIdentifier( YIdentifier id ) throws YPersistenceException {
        DAO dao = AbstractEngine.getDao();
        dao.save( id );
    }


    public YIdentifier createChild() throws YPersistenceException {
        YIdentifier identifier =
                new YIdentifier(this.id + "." + (_children.size() + 1));
        _children.add(identifier);
        identifier._parent = this;
        saveIdentifier( identifier );

        return identifier;
    }

    /**
     * Creates a child identifier.
     *
     * @param childNum
     * @return the child YIdentifier object with id == childNum
     */
    public YIdentifier createChild(int childNum) throws YPersistenceException {
        if (childNum < 1) {
            throw new IllegalArgumentException("Childnum must > 0");
        }
        String childNumStr = "" + childNum;
        for (int i = 0; i < _children.size(); i++) {
            YIdentifier identifier = _children.get(i);
            String exisitingChildNumString = identifier.toString();
            String lastPartOfExisistingChildNumString =
                    exisitingChildNumString.substring(exisitingChildNumString.lastIndexOf('.') + 1);
            if (childNumStr.equals(lastPartOfExisistingChildNumString)) {
                throw new IllegalArgumentException("" +
                        "Childnum uses an int already being used.");
            }
        }
        YIdentifier identifier =
                new YIdentifier(this.id + "." + childNumStr);
        _children.add(identifier);
        identifier._parent = this;

        saveIdentifier( identifier );
        
        return identifier;
    }


    
    @ManyToOne
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="parent")
    public YIdentifier getParent() {
        return _parent;
    }
    
    @ManyToOne
    @OnDelete(action=OnDeleteAction.CASCADE)
    @JoinColumn(name="parent")
    protected void setParent(YIdentifier parent) {
    	_parent = parent;
    }

    @Transient
    public boolean isImmediateChildOf(YIdentifier identifier) {
        return this._parent == identifier;
    }


    @Override
	public String toString() {
    	if( id == null ) return "null" + hashCode();
        return this.id;
    }

    @Transient
    public synchronized List<YNetElement> getLocationsForNet(YNet net) {
    	List<YNetElement> retval = new LinkedList<YNetElement>();

    	List l = net.getNetElements();

    	for (int i = 0; i < l.size(); i++) {
    		YNetElement elem = (YNetElement) l.get(i);
    		if (elem instanceof YCondition) {
    			if (((YCondition) elem).contains(this)) {
    				for (int j = 0; j < ((YCondition) elem).getAmount(this); j++) {
    					retval.add(elem);
    				}
    			}    			
    		} else if (elem instanceof YTask) {
				YIdentifier contained = ((YTask) elem).getContainingIdentifier();
    			if (contained!=null && contained.equals(this)) {
    				retval.add(elem);    				
    			}
    		}
    	}
    	
    	// TODO Why is this synchronized?  -- DM
        return retval;
    }
    

    /**
     * @return
     */
    @Transient
    @Deprecated
    public synchronized List<YNetElement> getLocations() throws YPersistenceException {
    	List<YNetElement> retval = new LinkedList<YNetElement>();
        
        PropertyRestriction restriction = new PropertyRestriction("basicCaseId", PropertyRestriction.Comparison.EQUAL , this.toString());
        List<YNetRunner> runners = AbstractEngine.getDao().retrieveByRestriction(YNetRunner.class, restriction);
        
        if( runners.size()==0 && getParent() != null ) {
        	restriction = new PropertyRestriction("basicCaseId", PropertyRestriction.Comparison.EQUAL , this.getParent().toString());
            runners = AbstractEngine.getDao().retrieveByRestriction(YNetRunner.class, restriction);        	
        }
        
        if( runners.size() > 0 ) {
            YNetRunner runner = runners.get(0);
            
        	YNet net = runner.getNet();
        	List l = net.getNetElements();
            
        	for (int i = 0; i < l.size(); i++) {
        		YNetElement elem = (YNetElement) l.get(i);
        		if (elem instanceof YCondition) {
        			if (((YCondition) elem).contains(this)) {
        				for (int j = 0; j < ((YCondition) elem).getAmount(this); j++) {
        					retval.add(elem);
        				}
        			}    			
        		} else if (elem instanceof YTask) {
    				YIdentifier contained = ((YTask) elem).getContainingIdentifier();
        			if (contained!=null && contained.equals(this)) {
        				retval.add(elem);    				
        			}
        		}
        	}
        }
    	
    	// TODO Why is this synchronized?  -- DM
        return retval;
    }

    @Transient
    public YIdentifier getAncestor() {
        if (null != this.getParent()) {
            return getAncestor();
        } else
            return this;
    }


    @Override
	public boolean equals(Object another) {
        if (another.toString().equals(this.toString())) {
            return true;
        } else
            return false;
    }
    
    /*
     * Error log
     * */
    List<YErrorEvent> errors = new ArrayList();
    public void addError(YErrorEvent error ) {
    	errors.add(error);
    }
    
    @OneToMany(cascade = {CascadeType.ALL})
    public List<YErrorEvent> getErrors() {
    	return errors;
    }
    public void setErrors(List<YErrorEvent> errors) {
		this.errors = errors;
	}
}
