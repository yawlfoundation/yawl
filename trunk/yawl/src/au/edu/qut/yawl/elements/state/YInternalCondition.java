/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements.state;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import au.edu.qut.yawl.elements.YConditionInterface;
import au.edu.qut.yawl.elements.YExternalNetElement;
import au.edu.qut.yawl.elements.YTask;
import au.edu.qut.yawl.events.DispatcherFactory;
import au.edu.qut.yawl.events.StateEvent;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YStateException;

/**
 * 
 * @author Lachlan Aldred
 * Date: 24/04/2003
 * Time: 10:17:37
 * 
 */
@Entity
@DiscriminatorValue("internal_condition")
public class YInternalCondition extends YExternalNetElement implements YConditionInterface {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	
    private YIdentifierBag _bag;
    public YTask _myTask;
    
    private StateEvent state;
    
    public YInternalCondition(StateEvent state, YTask myTask) {
    	this.state = state;
        _bag = new YIdentifierBag(this);
        _myTask = myTask;
    }

    @OneToOne(cascade={CascadeType.ALL})
	@JoinTable(
			name = "IdentifierBagOneToOne",
			joinColumns = {@JoinColumn(name = "extern_id")},
			inverseJoinColumns = @JoinColumn(name = "bag_id"))
    private YIdentifierBag getIdentifierBag() {
    	return _bag;
    }
    
    private void setIdentifierBag(YIdentifierBag bag) {
    	_bag = bag;
    }


    @OneToOne
    private YTask getTask() {
    	return _myTask;
    }
    
    private void setTask(YTask task) {
    	_myTask = task;
    }

    /**
     * Adds an identifier to the collection.
     * @param identifier
     */
    public void add(YIdentifier identifier) throws YPersistenceException {
    	DispatcherFactory.getConsoleDispatcher().fireEvent( state );
        _bag.addIdentifier(identifier);
    }

    /**
     * @param identifier
     * @return true iff this contains identifier.
     */
    public boolean contains(YIdentifier identifier) {
        return _bag.contains(identifier);
    }

    /**
     * @return true iff this contains one or more identifier.
     */
    public boolean containsIdentifier() {
        return _bag.getIdentifiers().size() > 0;
    }

    /**
     * Get the number of identifier matching the the one passed in.
     * @param identifier
     * @return the number of equal identifiers in this.
     */
    public int getAmount(YIdentifier identifier) {
        return _bag.getAmount(identifier);
    }

    /**
     * @return a List of the identifiers in the condition numbering 1 or more.
     */
    @Transient
    public List getIdentifiers() {
        return _bag.getIdentifiers();
    }

    /**
     * Removes one YIdentifier from this condition.  If there are none
     * inside then make no change to the state of this.
     */
    public YIdentifier removeOne() throws YPersistenceException {
    	DispatcherFactory.getConsoleDispatcher().fireEvent( state );
        YIdentifier id = (YIdentifier) getIdentifiers().get(0);
        _bag.remove(id, 1);
        return id;
    }

    /**
     * Removes one YIdentifier equal to identifier from the condition. If there are none
     * inside then make no change to the state of this.
     * @param identifier
     */
    public void removeOne(YIdentifier identifier) throws YPersistenceException {
    	DispatcherFactory.getConsoleDispatcher().fireEvent( state );
        _bag.remove(identifier, 1);
    }

    /**
     * Remove from this amount YIdentifiers equal to identifier.
     * @param identifier
     * @param amount the amount to remove.
     * @throws YStateException iff amount is greater than the number of YIdentifiers
     * held inside this, and further more no change will be made to the state of this.
     */
    public void remove(YIdentifier identifier, int amount) throws YStateException, YPersistenceException {
    	DispatcherFactory.getConsoleDispatcher().fireEvent( state );
        _bag.remove(identifier, amount);
    }

    /**
     * Removes all the YIdentifiers equal to identifier.
     * @param identifier
     */
    public void removeAll(YIdentifier identifier) throws YPersistenceException {
    	DispatcherFactory.getConsoleDispatcher().fireEvent( state );
        _bag.remove(identifier, _bag.getAmount(identifier));
    }

    public void removeAll() {
    	DispatcherFactory.getConsoleDispatcher().fireEvent( state );
        _bag.removeAll();
    }

    public String toString() {
        return state.getState() + "[" + _myTask.toString() + "]";
    }
}
