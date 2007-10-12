/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.state;

import au.edu.qut.yawl.elements.YCondition;
import au.edu.qut.yawl.elements.YConditionInterface;
import au.edu.qut.yawl.elements.YInputCondition;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.engine.YPersistenceManager;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.logging.YawlLogServletInterface;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * 
 * This class has control over data structures that allow for
 * storing an identifer and manging a set of children.
 * @author Lachlan Aldred
 * 
 */
public class YIdentifier {
    /*************************/
    /* INSERTED VARIABLES AND METHODS FOR PERSISTANCE* /
    /**************************/
    private List locationNames = new Vector();
    private String id = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List getLocationNames() {
        return locationNames;
    }

    public void setLocationNames(List names) {
        this.locationNames = names;
    }

    public String get_idString() {
        return _idString;
    }

    public void set_idString(String id) {
        this._idString = id;
    }

    public void set_children(List children) {
        this._children = children;
    }

    public List get_children() {
        return _children;
    }

    public YIdentifier get_father() {
        return _father;
    }

    public void set_father(YIdentifier father) {
        _father = father;
    }

    /************************************************/

    private List _locations = new Vector();
    private String _idString;
    private List _children = new Vector();
    private YIdentifier _father;


    public YIdentifier() {
        _idString = YawlLogServletInterface.getInstance().getNextCaseId();
        //_idString = "" + _count++;
    }


    /**
     * Constructor Identifier.
     * @param idString

     Changed to public for persistance
     */
    public YIdentifier(String idString) {
        _idString = idString;
    }


    public List getChildren() {
        return _children;
    }


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


    public YIdentifier createChild(YPersistenceManager pmgr) throws YPersistenceException {
        YIdentifier identifier =
                new YIdentifier(this._idString + "." + (_children.size() + 1));
        _children.add(identifier);
        identifier._father = this;

        /*
          INSERTED FOR PERSISTANCE
         */
        if (pmgr != null) {
//            YPersistance.getInstance().storeData(identifier);
//            YPersistance.getInstance().updateData(this);
            pmgr.storeObjectFromExternal(identifier);
            pmgr.updateObjectExternal(this);
        }
        return identifier;
    }

    /**
     * Creates a child identifier.
     *
     * @param childNum
     * @return the child YIdentifier object with id == childNum
     */
    public YIdentifier createChild(YPersistenceManager pmgr, int childNum) throws YPersistenceException {
        if (childNum < 1) {
            throw new IllegalArgumentException("Childnum must > 0");
        }
        String childNumStr = "" + childNum;
        for (int i = 0; i < _children.size(); i++) {
            YIdentifier identifier = (YIdentifier) _children.get(i);
            String exisitingChildNumString = identifier.toString();
            String lastPartOfExisistingChildNumString =
                    exisitingChildNumString.substring(exisitingChildNumString.lastIndexOf('.') + 1);
            if (childNumStr.equals(lastPartOfExisistingChildNumString)) {
                throw new IllegalArgumentException("" +
                        "Childnum uses an int already being used.");
            }
        }
        YIdentifier identifier =
                new YIdentifier(this._idString + "." + childNumStr);
        _children.add(identifier);
        identifier._father = this;

        /*
          INSERTED FOR PERSISTANCE
         */
//        YPersistance.getInstance().storeData(identifier);
//        YPersistance.getInstance().updateData(this);
        if (pmgr != null) {
            pmgr.storeObjectFromExternal(identifier);
            pmgr.updateObjectExternal(this);
        }

        return identifier;
    }


    public YIdentifier getParent() {
        return _father;
    }


    public boolean isImmediateChildOf(YIdentifier identifier) {
        return this._father == identifier;
    }


    public String toString() {
        return this._idString;
    }


    public synchronized void addLocation(YPersistenceManager pmgr, YConditionInterface condition) throws YPersistenceException {
        if (condition == null) {
            throw new RuntimeException("Cannot add null condition to this identifier.");
        }
        this._locations.add(condition);

        /*
          INSERTED FOR PERSISTANCE
         */
        if ((condition instanceof YCondition) && !(condition instanceof YInputCondition)) {

            this.locationNames.add(condition.toString().substring(condition.toString().indexOf(":") + 1, condition.toString().length()));
        } else {
            this.locationNames.add(condition.toString());
        }

//    YPersistance.getInstance().updateData(this);
        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
        }
    }


    public synchronized void removeLocation(YPersistenceManager pmgr, YConditionInterface condition) throws YPersistenceException {
        if (condition == null) {
            throw new RuntimeException("Cannot remove null condition from this identifier.");
        }

        this._locations.remove(condition);

        /*
          INSERTED FOR PERSISTANCE
         */
        if (condition instanceof YCondition && !(condition instanceof YInputCondition)) {
            this.locationNames.remove(condition.toString().substring(condition.toString().indexOf(":") + 1, condition.toString().length()));
        } else {
            this.locationNames.remove(condition.toString());
        }

//        YPersistance.getInstance().updateData(this);
        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
        }

    }




    public synchronized void addLocation(YPersistenceManager pmgr, YTask task) throws YPersistenceException {
        if (task == null) {
            throw new RuntimeException("Cannot add null task to this identifier.");
        }
        this._locations.add(task);

        /*
          INSERTED FOR PERSISTANCE
         */
        this.locationNames.add(task.getID());

//        YPersistance.getInstance().updateData(this);
        if (pmgr != null) {
            pmgr.updateObjectExternal(this);
        }
    }


    public synchronized void removeLocation(YPersistenceManager pmgr, YTask task) throws YPersistenceException {
        Logger.getLogger(this.getClass()).debug("--> removeLocation: TaskID = " + task.getID());

        if (task == null) {
            throw new RuntimeException("Cannot remove null task from this identifier.");
        }
        this._locations.remove(task);

        /*
          INSERTED FOR PERSISTANCE
         */
        this.locationNames.remove(task.getID());

        /**
         * AJH: Bugfix - YIdentifiers get deleted elsewhere, so don't attempt here
         */
//        if (pmgr != null) {
//            pmgr.updateObjectExternal(this);
//        }
    }


    public synchronized List getLocations() {
        return _locations;
    }

    public YIdentifier getAncestor() {
        if (null != this.getParent()) {
            return getAncestor();
        } else
            return this;
    }


    public boolean equals(Object another) {
        if (another.toString().equals(this.toString())) {
            return true;
        } else
            return false;
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
    public int hashCode()
    {
        return this.toString().hashCode();
    }
}
