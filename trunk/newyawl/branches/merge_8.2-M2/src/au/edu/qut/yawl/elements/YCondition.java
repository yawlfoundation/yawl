/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YPersistenceManager;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.util.YIdentifierBag;

import java.util.List;
import org.apache.log4j.Logger;

/**
 * 
 * An external condition is equivalent to a condition in the YAWL paper.
 * @author Lachlan Aldred
 * 
 */
public class YCondition extends YExternalNetElement implements YConditionInterface {


    protected YIdentifierBag _bag;
    private boolean _isImplicit;
    private Logger logger;

    public YCondition(String id, String label, YNet container) {
        super(id, container);
        logger = Logger.getLogger(this.getClass());
        _name = label;
        _bag = new YIdentifierBag(this);
    }


    public YCondition(String id, YNet container) {
        super(id, container);
        logger = Logger.getLogger(this.getClass());
        _bag = new YIdentifierBag(this);
    }


    public void setImplicit(boolean isImplicit) {
        _isImplicit = isImplicit;
    }


    public boolean isImplicit() {
        return _isImplicit;
    }


    public List verify() {
        return super.verify();
    }


    public boolean isAnonymous() {
        return _name == null;
    }


    public void add(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException {
        logger.debug("*** YCondition: Adding " + identifier + " to bag with " + identifier.getLocations().size() + " locations");
        _bag.addIdentifier(pmgr, identifier);
    }

    public boolean contains(YIdentifier identifier) {
        return _bag.contains(identifier);
    }

    public boolean containsIdentifier() {
        return _bag.getIdentifiers().size() > 0;
    }

    public int getAmount(YIdentifier identifier) {
        return _bag.getAmount(identifier);
    }

    public List getIdentifiers() {
        return _bag.getIdentifiers();
    }

    public YIdentifier removeOne(YPersistenceManager pmgr) throws YPersistenceException {
        YIdentifier identifier = (YIdentifier) getIdentifiers().get(0);
        _bag.remove(pmgr, identifier, 1);
        return identifier;
    }

    public void removeOne(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException {
        _bag.remove(pmgr, identifier, 1);
    }

    public void remove(YPersistenceManager pmgr, YIdentifier identifier, int amount) throws YPersistenceException {
        _bag.remove(pmgr, identifier, amount);
    }

    public void removeAll(YPersistenceManager pmgr, YIdentifier identifier) throws YPersistenceException {
        _bag.remove(pmgr, identifier, _bag.getAmount(identifier));
    }

    public synchronized void removeAll(YPersistenceManager pmgr) {
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
