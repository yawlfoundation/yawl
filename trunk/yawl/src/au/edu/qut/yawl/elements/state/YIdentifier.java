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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import au.edu.qut.yawl.elements.YConditionInterface;
import au.edu.qut.yawl.elements.YNet;
import au.edu.qut.yawl.elements.YNetElement;
import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.events.YErrorEvent;
import au.edu.qut.yawl.events.YawlEventLogger;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.engine.domain.YWorkItemRepository;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.managed.DataContext;
import au.edu.qut.yawl.persistence.managed.DataProxy;
import au.edu.qut.yawl.persistence.managed.DataProxyStateChangeListener;

/**
 * 
 * This class has control over data structures that allow for
 * storing an identifer and manging a set of children.
 * @author Lachlan Aldred
 * @hibernate.class table="identifiers"
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
    //private List<String> locationNames = new Vector<String> ();
    private String id = null;

    /**
     * @hibernate.id
     */
    @Id
    @Column(name="identifier_id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
/*
    @CollectionOfElements
    public List<String> getLocationNames() {
        return locationNames;
    }

    public void setLocationNames(List<String> names) {
        this.locationNames = names;
    }
    */
    /************************************************/

    private List<YNetElement> _locations = new Vector<YNetElement>();
    private List<YIdentifier> _children = new Vector<YIdentifier>();
    private YIdentifier _parent;


    public YIdentifier() {
//    	 TODO This is bad style, primary key generation should be located elsewhere!!
    	//System.err.println( "FIXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX-ME" );
        id = YawlEventLogger.getInstance().getNextCaseId();
  	
    }

    public YIdentifier(String idString) {
        id = idString;
    }

    @OneToMany(mappedBy="parent",cascade={CascadeType.REMOVE})
    public List<YIdentifier> getChildren() {
        return _children;
    }
    
    protected void setChildren(List<YIdentifier> children) {
    	_children = children;
    }

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
    
    public static void saveIdentifier( YIdentifier id, YIdentifier parent,
    		DataProxyStateChangeListener listener ) throws YPersistenceException {

    	DataContext context = AbstractEngine.getDataContext();
    	DataProxy proxy = context.getDataProxy( id );
    	DataProxy parentProxy = null;
    	if( parent != null ) {
    		parentProxy = context.getDataProxy( parent );
    		assert parentProxy != null : "attempting to persist child when parent is not persisted!";
    	}
    	if( proxy == null ) {
    		proxy = context.createProxy( id, listener );
    		context.attachProxy( proxy, id, parentProxy );
    	}
        if( parent != null ) {
        	context.save( parentProxy );
        }
        context.save( proxy );
    }


    public YIdentifier createChild() throws YPersistenceException {
        YIdentifier identifier =
                new YIdentifier(this.id + "." + (_children.size() + 1));
        _children.add(identifier);
        identifier._parent = this;
        saveIdentifier( identifier, this, null );

        /*
          INSERTED FOR PERSISTANCE
         */
//        if (pmgr != null) {
////            YPersistance.getInstance().storeData(identifier);
////            YPersistance.getInstance().updateData(this);
//            pmgr.storeObjectFromExternal(identifier);
//            pmgr.updateObjectExternal(this);
//        }
//        DaoFactory.createYDao().create(this);
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

        saveIdentifier( identifier, this, null );
        
        /*
          INSERTED FOR PERSISTANCE
         */
//        YPersistance.getInstance().storeData(identifier);
//        YPersistance.getInstance().updateData(this);
//  TODO      if (pmgr != null) {
//            pmgr.storeObjectFromExternal(identifier);
//            pmgr.updateObjectExternal(this);
//        }

        return identifier;
    }


    
    @ManyToOne
    @OnDelete(action=OnDeleteAction.CASCADE)
    public YIdentifier getParent() {
        return _parent;
    }
    
    @ManyToOne
    @OnDelete(action=OnDeleteAction.CASCADE)
    protected void setParent(YIdentifier parent) {
    	_parent = parent;
    }

    @Transient
    public boolean isImmediateChildOf(YIdentifier identifier) {
        return this._parent == identifier;
    }


    @Override
	public String toString() {
    	if( id == null ) return "null";
        return this.id;
    }


    public synchronized void addLocation(YConditionInterface condition) throws YPersistenceException {
        if (condition == null) {
            throw new RuntimeException("Cannot add null condition to this identifier.");
        }
        this._locations.add((YNetElement) condition);

        /*
          INSERTED FOR PERSISTANCE
         
        if ((condition instanceof YCondition) && !(condition instanceof YInputCondition)) {

            this.locationNames.add(condition.toString().substring(condition.toString().indexOf(":") + 1, condition.toString().length()));
        } else {
            this.locationNames.add(condition.toString());
        }
*/
        //saveIdentifier( this, null, null );
//    YPersistance.getInstance().updateData(this);
// TODO       if (pmgr != null) {
//            pmgr.updateObjectExternal(this);
//        }
    }


    public synchronized void removeLocation(YConditionInterface condition) throws YPersistenceException {
        if (condition == null) {
            throw new RuntimeException("Cannot remove null condition from this identifier.");
        }

        this._locations.remove(condition);

        /*
          INSERTED FOR PERSISTANCE
         *//*
        if (condition instanceof YCondition && !(condition instanceof YInputCondition)) {
            this.locationNames.remove(condition.toString().substring(condition.toString().indexOf(":") + 1, condition.toString().length()));
        } else {
            this.locationNames.remove(condition.toString());
        }
*/
        //saveIdentifier( this, null, null );
//        YPersistance.getInstance().updateData(this);
// TODO       if (pmgr != null) {
//            pmgr.updateObjectExternal(this);
//        }

    }




    public synchronized void addLocation(YTask task) throws YPersistenceException {
        if (task == null) {
            throw new RuntimeException("Cannot add null task to this identifier.");
        }
        this._locations.add(task);

        /*
          INSERTED FOR PERSISTANCE
         *//*
        this.locationNames.add(task.getSpecURI());
*/
        //saveIdentifier( this, null, null );
//        YPersistance.getInstance().updateData(this);
//  TODO      if (pmgr != null) {
//            pmgr.updateObjectExternal(this);
//        }
    }


    public synchronized void removeLocation( YTask task) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> removeLocation: TaskID = " + task.getID());

        if (task == null) {
            throw new RuntimeException("Cannot remove null task from this identifier.");
        }
        this._locations.remove(task);

        /*
          INSERTED FOR PERSISTANCE
         *//*
        this.locationNames.remove(task.getSpecURI());*/
    }

    //FIXME do we persist locations or not? (Lachlan?)
    /**
     * @return
     */
    @Transient
    //@OneToMany(cascade={CascadeType.ALL})
    //@OnDelete(action=OnDeleteAction.CASCADE)
    public synchronized List<YNetElement> getLocations() {
    	List<YNetElement> retval = new LinkedList<YNetElement>();

        List<DataProxy> runners2 = AbstractEngine.getDataContext().retrieveAll(YNetRunner.class, null);
        
        PropertyRestriction restriction = new PropertyRestriction("basicCaseId", PropertyRestriction.Comparison.EQUAL , this.toString());
        List<DataProxy> runners = AbstractEngine.getDataContext().retrieveByRestriction(YNetRunner.class, restriction ,null);

        if (runners.size()==0) {
        	restriction = new PropertyRestriction("basicCaseId", PropertyRestriction.Comparison.EQUAL , this.getParent().toString());
            runners = AbstractEngine.getDataContext().retrieveByRestriction(YNetRunner.class, restriction ,null);        	
        }
        
        YNetRunner runner = (YNetRunner) runners.get(0).getData();
        
//        YNetRunner runner = YWorkItemRepository.getInstance().getNetRunner(this);
//   	if (runner==null) {
//    		runner = YWorkItemRepository.getInstance().getNetRunner(this.getParent());
//    	}

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
    	
    	// TODO Why is this synchronized?  -- DM
        return retval;
    }
//    public void setLocations(List<YNetElement> locs) {
//    	_locations = locs;
//    }

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

    /**
     * Returns a hash code value for the object. This method is
     * supported for the benefit of hashtables such as those provided by
     * <code>java.util.Hashtable</code>.
     * <p/>
     * The general contract of <code>hashCode</code> is:
     * <ul>
     * <li>Whenever it is invoked on the same object more than once during
     * an execution of a Java application, the <tt>hashCode</tt> method
     * must consistently return the same integer, provided no information
     * used in <tt>equals</tt> comparisons on the object is modified.
     * This integer need not remain consistent from one execution of an
     * application to another execution of the same application.
     * <li>If two objects are equal according to the <tt>equals(Object)</tt>
     * method, then calling the <code>hashCode</code> method on each of
     * the two objects must produce the same integer result.
     * <li>It is <em>not</em> required that if two objects are unequal
     * according to the {@link Object#equals(Object)}
     * method, then calling the <tt>hashCode</tt> method on each of the
     * two objects must produce distinct integer results.  However, the
     * programmer should be aware that producing distinct integer results
     * for unequal objects may improve the performance of hashtables.
     * </ul>
     * <p/>
     * As much as is reasonably practical, the hashCode method defined by
     * class <tt>Object</tt> does return distinct integers for distinct
     * objects. (This is typically implemented by converting the internal
     * address of the object into an integer, but this implementation
     * technique is not required by the
     * Java<font size="-2"><sup>TM</sup></font> programming language.)
     *
     * @return a hash code value for this object.
     * @see Object#equals(Object)
     * @see java.util.Hashtable
     */
    @Override
	public int hashCode()
    {
        return this.toString().hashCode();
    }
}
