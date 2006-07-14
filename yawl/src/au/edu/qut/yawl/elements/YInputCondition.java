/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import java.util.List;
import java.util.Vector;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import au.edu.qut.yawl.util.YVerificationMessage;


/**
 * 
 * @author Lachlan Aldred
 * Date: 22/04/2003
 * Time: 13:43:54
 * 
 * 
 * Set class to non-final for purposes of hibernate
 * 
 * @hibernate.subclass discriminator-value="5"
 */
@Entity
@DiscriminatorValue("input_condition")
public class YInputCondition extends YCondition {
	/**
	 * One should only change the serialVersionUID when the class method signatures have changed.  The
	 * UID should stay the same so that future revisions of the class can still be backwards compatible
	 * with older revisions if method signatures have not changed. 
	 * Serial version format: year (4 digit) - month (2 digit) - yawl release version (4 digit)
	 */
	private static final long serialVersionUID = 2006030080l;
	

	/**
	 * Null constructor only used by hibernate
	 *
	 */
	protected YInputCondition() {
		super();
	}

    /**
     * Constructor.
     * @param id of the NetElement.
     * @param label of the condition.
     */
    public YInputCondition(String id, String label, YNet container) {
        super(id, label, container);
    }

    public YInputCondition(String id, YNet container) {
        super(id, container);
    }


    /**
     * This is one of those few cases where the sub-class has tighter constraints than the
     * parent class on one of the supertype members (preset).  ie. an InputCondition must always
     * have an empty preset.
     * @return a List of error messages.
     */
    public List verify() {
        List messages = new Vector();
/*        if(_preset.size() > 0 || _postset.size() > 0){
            if(this._preset.size() != 0){
                messages.add(new YVerificationMessage(this, this
                        + " preset must be empty: "+ this._preset.values()));
            }
            messages.addAll(verifyPostset());
        }
        else*/{
            if (getPresetElements().size() != 0) {
                messages.add(new YVerificationMessage(this, this
                        + " preset must be empty: " + getPresetElements(), YVerificationMessage.ERROR_STATUS));
            }
            messages.addAll(verifyPostsetFlows());
        }
        return messages;
    }


    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElement(this.getID()) != null) {
            return copyContainer.getNetElement(this.getID());
        }
        YInputCondition copy = (YInputCondition) super.clone();
        copy._net.setInputCondition(copy);
        return copy;
    }
}
