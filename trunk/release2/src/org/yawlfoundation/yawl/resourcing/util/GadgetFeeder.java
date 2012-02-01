/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.resourcing.util;

import org.yawlfoundation.yawl.engine.interfce.ServletUtils;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.resourcing.QueueSet;
import org.yawlfoundation.yawl.resourcing.ResourceManager;
import org.yawlfoundation.yawl.resourcing.WorkQueue;
import org.yawlfoundation.yawl.resourcing.jsf.comparator.WorkItemAgeComparator;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.util.PasswordEncryptor;

import java.util.Map;
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
            "<script type=\"text/javascript\">if (window.location.hash) { " +
            "var hash = window.location.hash; window.location.search = \"?\" + hash.substring(1);}</script> " + 
            "<script type=\"text/javascript\" src=\"http://www.yawlfoundation.org/gadgets/addclasskillclass.js\"></script>" +
            "<script type=\"text/javascript\" src=\"http://www.yawlfoundation.org/gadgets/attachevent.js\"></script>" +
            "<script type=\"text/javascript\" src=\"http://www.yawlfoundation.org/gadgets/semantictabset.js\"></script>";

    private final String _bodyPreamble =
            "<dl class=\"tabset\">\n" +
            "<dt class=\"ieclear\"></dt>  \n";

    private final String _bodyPostfix = "</dl>";

    private String _userid;
    private String _password;
    private String _rootURI;
    private String _parentURI;
    private String _encryptedPassword;
    private String _libs;
    private String _view;
    private boolean _altTabNames;
    private boolean _showSuspended;
    private Participant _participant = null;


//    public GadgetFeeder(HttpServletRequest req) {
//        _userid = req.getParameter("up_yawlUserID");
//        _password = req.getParameter("up_yawlPassword");
//        _rootURI = req.getParameter("up_tomcatHome");
//        _parentURI = req.getParameter("parent");
//        _libs = req.getParameter("libs");
//        _view = req.getParameter("view");
//        String altTabs = req.getParameter("up_altTabs");
//        _altTabNames = (altTabs != null) && altTabs.equals("1") ;
//        String showSusp = req.getParameter("up_showSusp");
//        _showSuspended = (showSusp != null) && showSusp.equals("1") ;
//        _participant = ResourceManager.getInstance().getParticipantFromUserID(_userid);
    public GadgetFeeder(Map req) {
        if (req != null) {
        _userid = getParam(req, "up_yawlUserID");
        _password = getParam(req, "up_yawlPassword");
        _rootURI = getParam(req, "up_tomcatHome");
        _parentURI = getParam(req, "parent");
        _libs = getParam(req, "libs");
        _view = getParam(req, "view");
        String altTabs = getParam(req, "up_altTabs");
        _altTabNames = (altTabs != null) && altTabs.equals("1") ;
        String showSusp = getParam(req, "up_showSusp");
        _showSuspended = (showSusp != null) && showSusp.equals("1") ;
        _participant = ResourceManager.getInstance().getParticipantFromUserID(_userid);
        }    
    }

    
    private String getParam(Map req, String name) {
        String[] value = (String[]) req.get(name);
        return (value != null) ? value[0] : null;
    }


    public String getFeed() {
        String errMsg = checkCredentials(_participant);
        if (errMsg != null) _participant = null;
        StringBuilder feed = new StringBuilder(_preamble);
        feed.append(getHead());
        feed.append(getBody(errMsg));
        feed.append("\n</html>");
        return feed.toString();
    }


    private String getHead() {
        StringBuilder head = new StringBuilder();
        head.append(_css);
        head.append(_script);
        head.append(getLibs());
        head.append(getTitleScript(_participant));
        head.append("</head>");
        return head.toString();
    }


    private String getBody(String errMsg) {
        StringBuilder body = new StringBuilder("<body>");
        if (errMsg != null) {
            body.append(getErrBody(errMsg));
        }
        else {
            body.append(getWorkLists(_participant));
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


    private String getLibs() {
        StringBuilder result = new StringBuilder();
        String[] libs = _libs.split(",");
        for (String lib : libs) {
             result.append("<script type=\"text/javascript\" src=\"http://www.google.com/ig/f/")
                   .append(lib)
                   .append("\"></script>\n");
        }
        return result.toString();
    }


    private String getTitleScript(Participant p) {
        StringBuilder result = new StringBuilder("<script type=\"text/javascript\">");
        result.append("function init() { _IG_SetTitle(\"YAWL Worklist");
        if (p != null) {
            result.append(": ")
                  .append(p.getFullName());
        }
        result.append("\"); }; _IG_RegisterOnloadHandler(init); </script>");
        return result.toString();       
    }


    private String getWorkLists(Participant p) {
        StringBuilder result = new StringBuilder(_bodyPreamble);
        QueueSet qSet = p.getWorkQueues();
        if (qSet != null) {
            int lastTab = getLastTab() ;
            for (int qType = WorkQueue.OFFERED; qType <= lastTab; qType++) {
                WorkQueue queue = qSet.getQueue(qType);
                result.append(getQueueContents(queue, qType));
            }
        }
        result.append(_bodyPostfix);
        return result.toString();
    }

    private String getQueueContents(WorkQueue queue, int qType) {
        StringBuilder result = new StringBuilder("<dt");
        if (qType == WorkQueue.OFFERED) result.append(" class=\"active\"");
        result.append(">");
        result.append(getTabName(qType, queue));
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


    private String getTabName(int qType, WorkQueue queue) {
        final String[] alts = { "Available", "Assigned", "In Progress", "Suspended" };
        String tabName = _altTabNames ? alts[qType] : WorkQueue.getQueueName(qType);
        if ((_view.equals("canvas")) || (! _showSuspended)) {
            int qSize = (queue != null) ? queue.getQueueSize() : 0 ;
            tabName += String.format(" (%d)", qSize);
        }
        return tabName;
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


    private int getLastTab() {
        return _showSuspended ? WorkQueue.SUSPENDED : WorkQueue.STARTED ;
    }

}