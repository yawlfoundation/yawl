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

import au.edu.qut.yawl.persistence.PolymorphicPersistableObject;
import au.edu.qut.yawl.util.YVerificationMessage;


/**
 * 
 * @author Lachlan Aldred
 * Date: 22/04/2003
 * Time: 13:45:29
 * 
 * 
 * Set class to non-final for purposes of hibernate
 * 
 * @hibernate.subclass discriminator-value="6"
 */
@Entity
@DiscriminatorValue("output_condition")
public class YOutputCondition extends YCondition implements PolymorphicPersistableObject {
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
	protected YOutputCondition() {
		super();
	}

    public YOutputCondition(String id, String label, YNet container) {
        super(id, label, container);

    }


    public YOutputCondition(String id, YNet container) {
        super(id, container);
    }


    public List verify() {
        List messages = new Vector();
/*        if(_postset.size() > 0 || _preset.size() > 0){
            if(this._postset.size() != 0){
                messages.add(new YVerificationMessage(this, this
                        + " postset must be empty: " + _postset.values()));
            }
            messages.addAll(verifyPreset());
        }
        else*/{
            if (getPostsetElements().size() != 0) {
                messages.add(new YVerificationMessage(this, this
                        + " postset must be empty: " + getPostsetElements(), YVerificationMessage.ERROR_STATUS));
            }
            messages.addAll(verifyPresetFlows());
        }
        return messages;
    }


/*    public synchronized void add(YIdentifier identifier){
        _bag.addIdentifier(identifier);
/*        YNetRunner netRunner = YWorkItemRepository.getInstance().getNetRunner(identifier);
        if(netRunner != null){
            netRunner.completeWorkItemInTask()
        }* /
    }*/


    public Object clone() throws CloneNotSupportedException {
        YNet copyContainer = _net.getCloneContainer();
        if (copyContainer.getNetElements().containsKey(this.getID())) {
            return copyContainer.getNetElement(this.getID());
        }
        YOutputCondition copy = (YOutputCondition) super.clone();
        copy._net.setOutputCondition((YOutputCondition) copy);
        return copy;
    }
}
