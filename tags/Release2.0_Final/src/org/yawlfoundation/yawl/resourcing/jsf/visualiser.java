/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a
 * collaboration of individuals and organisations who are committed to improving
 * workflow technology.
 */

package org.yawlfoundation.yawl.resourcing.jsf;

import com.sun.rave.web.ui.appbase.AbstractPageBean;
import com.sun.rave.web.ui.component.*;
import org.jdom.Element;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.jsf.dynform.DynFormFactory;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.JDOMUtil;

import javax.faces.FacesException;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.ExternalContext;
import java.io.IOException;
import java.util.Set;

/*
 * visualser.java
 *
 * @author:  Michael Adams
 * Date: 04/05/2009
 */

public class visualiser extends AbstractPageBean {
    private int __placeholder;

    private void _init() throws Exception { }


    private Page page1 = new Page();

    public Page getPage1() { return page1; }

    public void setPage1(Page p) { page1 = p; }


    private Html html1 = new Html();

    public Html getHtml1() { return html1; }

    public void setHtml1(Html h) { html1 = h; }


    private Head head1 = new Head();

    public Head getHead1() { return head1; }

    public void setHead1(Head h) { head1 = h; }


    private Link link1 = new Link();

    public Link getLink1() { return link1; }

    public void setLink1(Link l) { link1 = l; }


    private Body body1 = new Body();

    public Body getBody1() { return body1; }

    public void setBody1(Body b) { body1 = b; }


    private Form form1 = new Form();

    public Form getForm1() { return form1; }

    public void setForm1(Form f) { form1 = f; }


    private Button btnReturn = new Button();

    public Button getBtnReturn() { return btnReturn; }

    public void setBtnReturn(Button b) { btnReturn = b; }


    private Button btnView = new Button();

    public Button getBtnView() { return btnView; }

    public void setBtnView(Button b) { btnView = b; }


    private PanelLayout pnlContainer ;

    public PanelLayout getPnlContainer() { return pnlContainer; }

    public void setPnlContainer(PanelLayout pnl) { pnlContainer = pnl; }


    private HtmlOutputText outputText ;

    public HtmlOutputText getOutputText() { return outputText; }

    public void setOutputText(HtmlOutputText text) { outputText = text; }


    private HiddenField hdnSelectedItemID = new HiddenField();

    public HiddenField getHdnSelectedItemID() { return hdnSelectedItemID; }

    public void setHdnSelectedItemID(HiddenField hf) { hdnSelectedItemID = hf; }


    /****************************************************************************/

    public visualiser() { }

    /**
     * <p>Return a reference to each scoped bean.</p>
     */
    protected ApplicationBean getApplicationBean() {
        return (ApplicationBean)getBean("ApplicationBean");
    }

    protected RequestBean getRequestBean() {
        return (RequestBean)getBean("RequestBean");
    }

    protected SessionBean getSessionBean() {
        return (SessionBean)getBean("SessionBean");
    }


    public void init() {
        super.init();

        // *Note* - this logic should NOT be modified
        try {
            _init();
        } catch (Exception e) {
            log("selectUser Initialization Failure", e);
            throw e instanceof FacesException ? (FacesException) e: new FacesException(e);
        }
    }

    public void preprocess() { }

    public void prerender() {
        _sb.checkLogon();                                  // check session still live
        msgPanel.show(20, 0, "relative");                  // show msgs (if any)
        if (_sb.isWirEdit()) postEditWIR() ;
    }

    public void destroy() { }

    /*********************************************************************/

    private SessionBean _sb = getSessionBean();
    private MessagePanel msgPanel = _sb.getMessagePanel() ;
    private ResourceManager _rm = getApplicationBean().getResourceManager();


    public String btnReturn_action() {
        return "showDefaultQueues" ;
    }


    public String btnView_action() {
        String itemID = (String) hdnSelectedItemID.getValue();
        if (itemID == null) {
            msgPanel.error("No work item selected. Please select a work item to view.");
            return null;
        }
        
        WorkItemRecord selectedWIR = getSelectedWIR(itemID);

        // maybe the wir isn't started or was part of a cancellation set and now it's gone
        if (selectedWIR == null) {
            msgPanel.error("Cannot view item contents - it appears that the " +
                           "selected item may have been removed or cancelled. " +
                           "Please see the log files for details.");
            return null;
        }

        _sb.setDynFormType(ApplicationBean.DynFormType.tasklevel);
        DynFormFactory df = (DynFormFactory) getBean("DynFormFactory");
        df.setHeaderText("Edit Work Item: " + selectedWIR.getCaseID());
        df.setDisplayedWIR(selectedWIR);
        if (df.initDynForm("YAWL 2.0 - Edit Work Item")) {
            _sb.setVisualiserReferred(true);
            _sb.setVisualiserEditedWIR(selectedWIR);
            return "showDynForm" ;
        }
        else {
            msgPanel.error("Cannot view item contents - problem initialising " +
                           "dynamic form from task specification. " +
                           "Please see the log files for details.");
            return null;
        }
    }


        /** updates a workitem after editing on a dynamic form */
    private void postEditWIR() {
        if (_sb.isWirEdit()) {
            WorkItemRecord wir = _sb.getVisualiserEditedWIR();
            if (wir != null) {
                Element data = JDOMUtil.stringToElement(
                        ((DynFormFactory) getBean("DynFormFactory")).getDataList());
                wir.setUpdatedData(data);
                _rm.getWorkItemCache().update(wir) ;

                if (_sb.isCompleteAfterEdit()) {
                    completeWorkItem(wir, _sb.getParticipant());
                }
            }
            else {
                msgPanel.error("Could not complete workitem. Check log for details.");
            }
        }
        _sb.setWirEdit(false);
        _sb.setCompleteAfterEdit(false);
        _sb.setVisualiserReferred(false);
        _sb.setVisualiserEditedWIR(null);
        if (msgPanel.hasMessage()) forceRefresh();
    }


    private void completeWorkItem(WorkItemRecord wir, Participant p) {
        String result = _rm.checkinItem(p, wir, _sb.getSessionhandle());
        if (_rm.successful(result))
            _sb.removeWarnedForNonEdit(wir.getID());
        else
            msgPanel.error(msgPanel.format(result)) ;
    }


    
    public String getUsername() {
        Participant p = _sb.getParticipant();
        return (p != null) ? p.getUserID() : "";
    }


    public String getPassword() {
        Participant p = _sb.getParticipant();
        return (p != null) ? p.getPassword() : ""; 
    }

    
    /** refreshes the page */
    public void forceRefresh() {
        ExternalContext externalContext = getFacesContext().getExternalContext();
        if (externalContext != null) {
            try {
                externalContext.redirect("visualiser.jsp");
            }
            catch (IOException ioe) {
                // ok - do nothing
            }
        }
    }

    // it was necessary to do it this way rather than using 'jsp:plugin' in the
    // jsf because the 'codebase' parameter does not support dynamic value setting
    public String getAppletHtml() {
        String baseURI = getApplicationBean().getResServiceBaseURI();
        Participant p = _sb.getParticipant();
        StringBuilder result = new StringBuilder("<applet width=\"800\" height=\"600\"");
        result.append(" archive=\"visualiser.jar,javax.servlet.jar,jdom.jar,")
              .append(" resourceService.jar,saxon9.jar,log4j-1.2.14.jar\"")
              .append(" codebase=\"")
              .append(baseURI)
              .append("/visualiserApplet\"")
              .append(" code=\"worklist.WRKLApplet.class\" MAYSCRIPT>")
              .append(" <param name=\"user\" value=\"")
              .append(p.getUserID())
              .append("\"/>")
              .append(" <param name=\"pass\" value=\"")
              .append(p.getPassword())
              .append("\"/>")
              .append(" <param name=\"urYAWL\" value=\"")
              .append(baseURI)
              .append("\"/>")
              .append("</applet>");

        return result.toString();
    }


    private WorkItemRecord getSelectedWIR(String itemID) {
        Set<WorkItemRecord> wirSet = _sb.getQueue(WorkQueue.STARTED);
        WorkItemRecord selectedWIR = null;
        for (WorkItemRecord wir : wirSet) {
            if (wir.getID().equals(itemID)) {
                selectedWIR = wir ;
                break ;
            }
        }
        return selectedWIR;
    }

}