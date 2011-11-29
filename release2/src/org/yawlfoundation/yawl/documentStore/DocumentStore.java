/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.documentStore;

import org.hibernate.ObjectNotFoundException;
import org.yawlfoundation.yawl.util.HibernateEngine;
import org.yawlfoundation.yawl.util.Sessions;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Michael Adams
 * @date 18/11/11
 */
public class DocumentStore extends HttpServlet {
    
    private Sessions _sessions;
    private HibernateEngine _db;
    private String _engineLogonName;
    private String _engineLogonPassword;


    public void init() {
        
        // setup database connection
        Set<Class> persistedClasses = new HashSet<Class>();
        persistedClasses.add(YDocument.class);
        _db = HibernateEngine.getInstance(true, persistedClasses);
        
        // set up session connections
        ServletContext context = getServletContext();
        _engineLogonName = context.getInitParameter("EngineLogonUserName");
        _engineLogonPassword = context.getInitParameter("EngineLogonPassword");
        String iaURI = context.getInitParameter("InterfaceA_Backend");
        _sessions = new Sessions();
        _sessions.setupInterfaceA(iaURI, _engineLogonName, _engineLogonPassword);
    }


    public void destroy() {
        _db.closeFactory();
        _sessions.shutdown();
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
                                throws IOException, ServletException {
        doPost(req, res);                                // redirect all GETs to POSTs
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
                               throws IOException {
        try {
            DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(req.getInputStream()));
            String action = dis.readUTF();
            String handle = dis.readUTF();
            String result = null;

            if (action == null) {
                throw new IOException("action is null");
            }
            else if (action.equals("connect")) {
                String userid = dis.readUTF();
                String password = dis.readUTF();
                result = _sessions.connect(userid, password);
            }
            else if (action.equals("checkConnection")) {
                result = String.valueOf(_sessions.checkConnection(handle));
            }
            else if (action.equals("disconnect")) {
                result = String.valueOf(_sessions.disconnect(handle));
            }
            else if (_sessions.checkConnection(handle)) {
                String caseID = dis.readUTF();
                long docID = dis.readLong();
                if (action.equals("get")) {
                    writeDocument(res, getDocument(docID));
                }
                else if (action.equals("put")) {
                    result = String.valueOf(putDocument(new YDocument(caseID, docID, dis)));
                }
                else if (action.equals("remove")) {
                    result = String.valueOf(removeDocument(docID));
                }
                else if (action.equals("clearcase")) {
                    result = clearCase(caseID);
                }
            }
            else writeString(res, "Invalid or disconnected session handle", "failure");

            if (result != null) writeString(res, result, "response");
        }
        catch (IOException ioe) {
            writeString(res, ioe.getMessage(), "failure");
        }
    }


    
    private void writeDocument(HttpServletResponse res, YDocument doc) throws IOException {
        if (doc != null) {
            res.setContentType("multipart/form-data");
            res.setBufferSize(doc.getDocumentSize());
            ServletOutputStream out = res.getOutputStream();
            out.write(doc.getDocument());
            out.flush();
            out.close();
        }
    }


    private void writeString(HttpServletResponse res, String msg, String tag)
            throws IOException {
        if (msg != null) {
            res.setContentType("text/xml; charset=UTF-8");
            OutputStreamWriter out = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
            out.write(responseMsg(msg, tag));
            out.flush();
            out.close();
        }
    }
    
    
    private String responseMsg(String core, String tag) {
        return StringUtil.wrap(core, tag);
    }


    private YDocument getDocument(long id) throws IOException {
        try {
            return (YDocument) _db.load(YDocument.class, id);
        }
        catch (ObjectNotFoundException onfe) {
            throw new IOException("No stored document found with id: " + id);
        }
    }


    private long putDocument(YDocument doc) throws IOException {
        if (doc.getDocumentSize() > 0) {
            if (doc.hasValidId()) {
                YDocument existingDoc = getDocument(doc.getId());
                existingDoc.setDocument(doc.getDocument());
                _db.exec(existingDoc, HibernateEngine.DB_UPDATE, true);
            }
            else {
                _db.exec(doc, HibernateEngine.DB_INSERT, true);
            }
            return doc.getId();
        }
        else throw new IOException("Could not read document from request stream");
    }


    private boolean removeDocument(long id) {
        try {
            YDocument doc = (YDocument) _db.load(YDocument.class, id);
            return (doc != null) && _db.exec(doc, HibernateEngine.DB_DELETE, true);
        }
        catch (ObjectNotFoundException onfe) {
            return false;
        }
    }
    
    
    private String clearCase(String id) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("delete from YDocument as yd where yd.caseId='").append(id).append("'");
        int rowsDeleted = _db.execUpdate(sb.toString(), true);
        sb.delete(0, sb.length());
        if (rowsDeleted > -1) {
            sb.append(rowsDeleted).append(" document")
              .append(rowsDeleted > 1 ? "s " : " ")
              .append("removed for case: ")
              .append(id);
        }
        else {
            sb.append("Error removing documents for case: ").append(id);
        }
        return sb.toString();
    }

}
