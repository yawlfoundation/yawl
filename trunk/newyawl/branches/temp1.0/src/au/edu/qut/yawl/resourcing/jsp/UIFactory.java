package au.edu.qut.yawl.resourcing.jsp;

import au.edu.qut.yawl.resourcing.WorkQueue;
import au.edu.qut.yawl.resourcing.resource.Participant;
import au.edu.qut.yawl.resourcing.resource.UserPrivileges;
import au.edu.qut.yawl.worklist.model.WorkItemRecord;

import java.util.Set;

/**
 * Created by IntelliJ IDEA. User: Default Date: 19/10/2007 Time: 17:56:28 To change this
 * template use File | Settings | File Templates.
 */
public class UIFactory {

    public UIFactory() {}

    public String getUI(String screen, Participant p) {
        if (screen.equals("offer")) return getOfferUI(p) ;
        else return null ;
    }


    public String getOfferUI(Participant p) {
        StringBuilder html = new StringBuilder();

        // build list box
        html.append("<div><select size='15' id='offerbox' align='right' ")
            .append("style='width:80%; height: 80%; padding: 0px; position:relative;' ")
            .append("onchange='showChoice(this)'> ") ;

        Set<WorkItemRecord> qSet =
                             p.getWorkQueues().getQueuedWorkItems(WorkQueue.OFFERED) ;

        for (WorkItemRecord wir: qSet) {
            String option = String.format("%s     %s      %s      %s", wir.getID(),
                                           wir.getTaskID(), wir.getStatus(),
                                           wir.getEnablementTime());
            html.append("<option>").append(option).append("</option>");
        }
        html.append("</select></div>");

        // insert buttons
        html.append("<div align='center'><button id='accept' dojoType='dijit.form.Button' ")
            .append("onClick='acceptOffer(this)' iconClass='plusIcon'>")
            .append("Accept Offer</button></div>");

        return html.toString();
    }
}
