package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.WorkItemAgeComparator;
import org.yawlfoundation.yawl.resourcing.resource.Participant;

import javax.servlet.http.HttpServletRequest;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Author: Michael Adams
 * Creation Date: 27/08/2009
 */
public class GadgetFeeder {
    
    private final String _preamble =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
            "<head>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />";

    private final String _css =
            "<link rel=\"stylesheet\" type=\"text/css\" href=\"http://www.yawlfoundation.org/gadgets/yworklist.css\"></link>";

    // these scripts enable a plain html tabset.
    // sourced from: http://phrogz.net/JS/SemanticTabset/semantictabset.html  
    private final String _script =
            "<script type=\"text/javascript\" src=\"http://www.yawlfoundation.org/gadgets/addclasskillclass.js\"></script>" +
            "<script type=\"text/javascript\" src=\"http://www.yawlfoundation.org/gadgets/attachevent.js\"></script>" +
            "<script type=\"text/javascript\" src=\"http://www.yawlfoundation.org/gadgets/semantictabset.js\"></script>";

    private final String _bodyPreamble =
            "<dl class=\"tabset\">\n" +
            "<dt class=\"ieclear\"></dt>  \n";

    private final String _bodyPostfix = "</dl>";

    
    private ResourceManager _rm;
    private String _userid;
    private String _password;
    private String _rootURI;
    private String _parentURI;
    private String _encryptedPassword;

    public GadgetFeeder(HttpServletRequest req) {
        _rm = ResourceManager.getInstance();
        _userid = req.getParameter("up_yawlUserID");
        _password = req.getParameter("up_yawlPassword");
        _rootURI = req.getParameter("up_tomcatHome");
        _parentURI = req.getParameter("parent");
    }


    public String getFeed() {
        StringBuilder feed = new StringBuilder(_preamble);
        feed.append(getHead());
        feed.append(getBody());
        feed.append("\n</html>");
        return feed.toString();
    }


    private String getHead() {
        StringBuilder head = new StringBuilder();
        head.append(_css);
        head.append(_script);
        head.append("</head>");
        return head.toString();
    }


    private String getBody() {
        StringBuilder body = new StringBuilder("<body>");
        Participant p = _rm.getParticipantFromUserID(_userid);
        String errMsg = checkCredentials(p);
        if (errMsg != null) {
            body.append(getErrBody(errMsg));
        }
        else {
            body.append(getWorkLists(p));
        }
        body.append("</body>");
        return body.toString();
    }


    private String checkCredentials(Participant p) {
        String errMsg = null;
        if (p != null) {
            try {
                _encryptedPassword = PasswordEncryptor.encrypt(_password);
                if (! p.getPassword().equals(_encryptedPassword)) {
                    errMsg = "Invalid Password.";
                }
            }
            catch (Exception e) {
                errMsg = e.getMessage();
            }
        }
        else errMsg = "Unknown Userid: " + _userid ;

        return errMsg;
    }


    private String getErrBody(String msg) {
        return "<div id=\"errMsg\" style=\"color:red;font-size:16px;font-style:bold\">" +
                msg + "</div>";
    }


    private String getWorkLists(Participant p) {
        StringBuilder result = new StringBuilder(_bodyPreamble);
        QueueSet qSet = p.getWorkQueues();
        if (qSet != null)
        for (int qType = WorkQueue.OFFERED; qType <= WorkQueue.SUSPENDED; qType++) {
            WorkQueue queue = qSet.getQueue(qType);
            result.append(getQueueContents(queue, qType));
        }
        result.append(_bodyPostfix);
        return result.toString();
    }

    private String getQueueContents(WorkQueue queue, int qType) {
        StringBuilder result = new StringBuilder("<dt");
        if (qType == WorkQueue.OFFERED) result.append(" class=\"active\"");
        result.append(">");
        result.append(WorkQueue.getQueueName(qType));
        result.append("</dt><dd>");
        if (queue != null) {
            result.append("<table cellpadding=\"1\">");
            SortedSet<WorkItemRecord> wirSet =
                    new TreeSet<WorkItemRecord>(new WorkItemAgeComparator());
            wirSet.addAll(queue.getAll());
            for (WorkItemRecord wir : wirSet) {
                result.append("<tr><td><a href=\"")
                      .append(_rootURI)  
                      .append("resourceService/faces/rssFormViewer.jsp")
                      .append(getParams(wir.getID()))
                      .append("\" target=\"_blank\">")
                      .append(wir.getIDForDisplay())
                      .append("</a></td></tr>");
            }
            result.append("</table>");
        }
        result.append("</dd>");
        return result.toString();
    }


    private String getParams(String itemid) {
        StringBuilder result = new StringBuilder("?itemid=");
        result.append(ServletUtils.urlEncode(itemid))
              .append("&amp;userid=")
              .append(ServletUtils.urlEncode(_userid))
              .append("&amp;password=")
              .append(ServletUtils.urlEncode(_encryptedPassword))
              .append("&amp;parent=")
              .append(ServletUtils.urlEncode(_parentURI));
        return result.toString();
    }

}