/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.elements;

import au.edu.qut.yawl.util.YVerificationMessage;

import java.util.List;
import java.util.Vector;


/**
 * 
 * @author Lachlan Aldred
 * Date: 22/04/2003
 * Time: 13:45:29
 * 
 */
public final class YOutputCondition extends YCondition {


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
