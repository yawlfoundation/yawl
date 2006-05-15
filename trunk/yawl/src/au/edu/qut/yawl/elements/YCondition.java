/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.elements.state.YIdentifierBag;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;

/**
 * 
 * An external condition is equivalent to a condition in the YAWL paper.
 * @author Lachlan Aldred
 * 
 * 
 * ***************************************************************************************
 * 
 * a condition is like a place in Petri nets.  It is a sibling member to the tasks inside 
 * any net.  They typically sit between tasks, and store the process state 'identifiers' 
 * ('tokens' in Petri speak).  When two tasks are directly connected together in a YAWL 
 * process model (in the XML syntax), an "invisible" condition is created between the 
 * tasks, when the process is loaded into the engine.
 * 
 * @hibernate.subclass discriminator-value="4"
 */
@Entity
@DiscriminatorValue("condition")
public class YCondition extends YExternalNetElement implements YConditionInterface, PolymorphicPersistableObject  {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;


    protected YIdentifierBag _bag;
    private boolean _isImplicit;
    
    /**
     * Null constructor only used by hibernate
     *
     */
    protected YCondition() {
    	super();
    }

    public YCondition(String id, String label, YNet container) {
        super(id, container);
        _name = label;
        _bag = new YIdentifierBag(this);
    }


    public YCondition(String id, YNet container) {
        super(id, container);
        _bag = new YIdentifierBag(this);
    }

    public void setImplicit(boolean isImplicit) {
        _isImplicit = isImplicit;
    }


    @Column(name="implicit")
    public boolean isImplicit() {
        return _isImplicit;
    }


    public List verify() {
        return super.verify();
    }

    @Transient
    public boolean isAnonymous() {
        return _name == null;
    }


    public void add(YIdentifier identifier) throws YPersistenceException {
        _bag.addIdentifier(identifier);
    }

    @Transient
    public boolean contains(YIdentifier identifier) {
        return _bag.contains(identifier);
    }

    @Transient
    public boolean containsIdentifier() {
        return _bag.getIdentifiers().size() > 0;
    }

    @Transient
    public int getAmount(YIdentifier identifier) {
        return _bag.getAmount(identifier);
    }

    // FIXME
    @Transient
    public List getIdentifiers() {
        return _bag.getIdentifiers();
    }

    @Transient
    public YIdentifier removeOne() throws YPersistenceException {
        YIdentifier identifier = (YIdentifier) getIdentifiers().get(0);
        _bag.remove(identifier, 1);
        return identifier;
    }

    public void removeOne(YIdentifier identifier) throws YPersistenceException {
        _bag.remove(identifier, 1);
    }

    public void remove(YIdentifier identifier, int amount) throws YPersistenceException {
        _bag.remove(identifier, amount);
    }

    public void removeAll(YIdentifier identifier) throws YPersistenceException {
        _bag.remove(identifier, _bag.getAmount(identifier));
    }

    public synchronized void removeAll() {
        _bag.removeAll();
    }

    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YCondition copiedCondition = (YCondition) super.clone();
        copiedCondition._bag = new YIdentifierBag(copiedCondition);
        return copiedCondition;
    }


    public String toXML() {
        StringBuffer xml = new StringBuffer();
        if (this instanceof YInputCondition) {
            xml.append("<inputCondition");
        } else if (this instanceof YOutputCondition) {
            xml.append("<outputCondition");
        } else {
            xml.append("<condition");
        }
        xml.append(" id=\"" + getID() + "\">");
        xml.append(super.toXML());
        if (this instanceof YInputCondition) {
            xml.append("</inputCondition>");
        } else if (this instanceof YOutputCondition) {
            xml.append("</outputCondition>");
        } else {
            xml.append("</condition>");
        }
        return xml.toString();
    }
}
