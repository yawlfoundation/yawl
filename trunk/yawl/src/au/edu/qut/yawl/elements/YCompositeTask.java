/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import au.edu.qut.yawl.elements.state.YIdentifier;
import au.edu.qut.yawl.engine.YNetRunner;
import au.edu.qut.yawl.exceptions.YDataStateException;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YSchemaBuildingException;
import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;
import au.edu.qut.yawl.util.YVerificationMessage;

/**
 * 
 * A YCompositeTask object is the executable equivalent of the YCompositeTask
 * in the YAWL paper.   It has the same properties and behaviour.
 * @author Lachlan Aldred
 * 
 * 
 * ***************************************************************************************
 * 
 * a composite task represents a task that decomposes down to a sub-net 
 * (i.e. another YNet object)
 * 
 * Set class to non-final for purposes of hibernate
 * 
 * @hibernate.subclass discriminator-value="3"
 */
@Entity
@DiscriminatorValue("composite")
public class YCompositeTask extends YTask implements PolymorphicPersistableObject {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;

	/**
	 * Null constructor for hibernate
	 */
	protected YCompositeTask() {
		super();
	}

    public YCompositeTask(String id, int joinType, int splitType, YNet container) {
        super(id, joinType, splitType, container);
    }

    @Transient
    public List verify() {
        List messages = new Vector();
        messages.addAll(super.verify());
        if (_decompositionPrototype == null) {
            messages.add(new YVerificationMessage(this, this + " composite task must contain a net.", YVerificationMessage.ERROR_STATUS));
        }
        if (!(_decompositionPrototype instanceof YNet)) {
            messages.add(new YVerificationMessage(this, this + " composite task may not decompose to other than a net.", YVerificationMessage.ERROR_STATUS));
        }
        return messages;
    }


    @Transient
    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YCompositeTask copy = (YCompositeTask) super.clone();
        return copy;
    }


    /**
     * @param pmgr
     * @param id
     * @throws YDataStateException
     * @throws YSchemaBuildingException
     */
    protected synchronized void startOne(YIdentifier id) throws YDataStateException, YSchemaBuildingException, YPersistenceException {
        _mi_executing.add(id);
        _mi_entered.removeOne(id);
        //todo Creating anotehr YNetRunner thread

        YNetRunner netRunner = new YNetRunner(
                (YNet) _decompositionPrototype,
                this,
                id,
                getData(id));

        /*
          INSERTED FOR PERSISTANCE
         */
        //todo AJH Do we actually need this call here ????
//        YPersistance.getInstance().storeData(netRunner);

        netRunner.continueIfPossible();
        netRunner.start();
    }


    public synchronized void cancel() throws YPersistenceException {
        if (_i != null) {
            List activeChildIdentifiers = _mi_active.getIdentifiers();
            Iterator iter = activeChildIdentifiers.iterator();
            while (iter.hasNext()) {
                YIdentifier identifier = (YIdentifier) iter.next();
                YNetRunner netRunner = _workItemRepository.getNetRunner(identifier);
                if (netRunner != null) {
                    netRunner.cancel();
                }
            }
        }
        super.cancel();
    }
}
