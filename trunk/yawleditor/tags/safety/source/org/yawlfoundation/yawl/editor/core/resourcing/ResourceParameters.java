package org.yawlfoundation.yawl.editor.core.resourcing;

import org.jdom2.Element;
import org.jdom2.Namespace;
import org.yawlfoundation.yawl.elements.YAtomicTask;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.resourcing.TaskPrivileges;
import org.yawlfoundation.yawl.resourcing.interactions.AllocateInteraction;
import org.yawlfoundation.yawl.resourcing.interactions.ResourceParseException;
import org.yawlfoundation.yawl.resourcing.interactions.StartInteraction;
import org.yawlfoundation.yawl.util.JDOMUtil;

/**
 * @author Michael Adams
 * @date 14/06/12
 */
public class ResourceParameters {

    private YAtomicTask _task;

    private BaseOfferInteraction _offer;
    private AllocateInteraction _allocate;
    private StartInteraction _start;
    private BaseSecondaryResources _secondary;

    // user-task privileges
    private TaskPrivileges _privileges ;


    public ResourceParameters(YAtomicTask task) {
        _task = task;
        _offer = new BaseOfferInteraction(_task.getID()) ;
        _allocate = new AllocateInteraction(_task.getID());
        _start = new StartInteraction(_task.getID()) ;
        _secondary = new BaseSecondaryResources();
        _privileges = new TaskPrivileges(_task.getID());
        parse();
    }

    public YTask getTask() { return _task; }

    public BaseOfferInteraction getOffer() { return _offer; }

    public AllocateInteraction getAllocate() { return _allocate; }

    public StartInteraction getStart() { return _start; }

    public BaseSecondaryResources getSecondary() { return _secondary; }

    public TaskPrivileges getPrivileges() { return _privileges; }


    private void parse() {
        if (_task != null) {
            String resourceXML = _task.getResourcingXML();
            if (resourceXML != null) {
                parse(resourceXML);
            }
        }
    }

    /**
     * Parse the Element passed for task resourcing info and build the appropriate
     * objects.
     * @param eleSpec the [resourcing] section from a particular task definition
     * within a specification file.
     */
    public void parse(Element eleSpec) {
        if (eleSpec != null) {
            Namespace nsYawl = eleSpec.getNamespace() ;
            try {
                _offer.parse(eleSpec.getChild("offer", nsYawl), nsYawl) ;
                _allocate.parse(eleSpec.getChild("allocate", nsYawl), nsYawl) ;
                _start.parse(eleSpec.getChild("start", nsYawl), nsYawl) ;
                _secondary.parse(eleSpec.getChild("secondary", nsYawl), nsYawl);
                _privileges.parse(eleSpec.getChild("privileges", nsYawl), nsYawl) ;
            }
            catch (ResourceParseException rpe) {
                // "Error parsing resourcing specification for task: " + _taskID
            }
        }
    }


    public void parse(String xml) {
        parse(JDOMUtil.stringToElement(xml));
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder("<resourcing>");
        xml.append(_offer.toXML()) ;
        xml.append(_allocate.toXML()) ;
        xml.append(_start.toXML()) ;
        xml.append(_secondary.toXML());
        xml.append(_privileges.toXML()) ;
        xml.append("</resourcing>");
        return xml.toString() ;
    }

}
