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

package org.yawlfoundation.yawl.documentStore;

import org.apache.log4j.Logger;
import org.hibernate.ObjectNotFoundException;
import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;
import org.yawlfoundation.yawl.util.HibernateEngine;
import org.yawlfoundation.yawl.util.Sessions;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.*;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * A storage cache for binary files passed as work item data.
 *
 * @author Michael Adams
 * @date 18/11/11
 */
public class DocumentStore extends YHttpServlet {

    private Sessions _sessions;            // maintains sessions with external services
    private HibernateEngine _db;           // communicates with underlying database
    private boolean _retainWhenCaseCompletes;


    public void init() {
        ServletContext context = getServletContext();

        // check size-fix for H2 databases
        fixH2BinarySize(context);

        // setup database connection
        Set<Class> persistedClasses = new HashSet<Class>();
        persistedClasses.add(YDocument.class);
        _db = HibernateEngine.getInstance(true, persistedClasses);


        // set up session connections
        _sessions = new Sessions();
        _sessions.setupInterfaceA(
                context.getInitParameter("InterfaceA_Backend"),
                context.getInitParameter("EngineLogonUserName"),
                context.getInitParameter("EngineLogonPassword"));

        // set retention flag
        String retain = context.getInitParameter("RetainStoredDocsOnCaseCompletion");
        _retainWhenCaseCompletes = (retain != null) && retain.equalsIgnoreCase("true");
    }


    public void destroy() {
        if (_db != null) _db.closeFactory();
        if (_sessions != null) _sessions.shutdown();
        super.destroy();
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        doPost(req, res);                                // redirect all GETs to POSTs
    }


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        try {

            // all request parameters are passed via the request's input stream
            DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(req.getInputStream()));
            String action = dis.readUTF();
            String handle = dis.readUTF();
            String result = null;

            if (action == null) {
                throw new IOException("action is null");
            } else if (action.equals("connect")) {
                String userid = dis.readUTF();
                String password = dis.readUTF();
                result = _sessions.connect(userid, password);
            } else if (action.equals("checkConnection")) {
                result = String.valueOf(_sessions.checkConnection(handle));
            } else if (action.equals("disconnect")) {
                result = String.valueOf(_sessions.disconnect(handle));
            } else if (_sessions.checkConnection(handle)) {
                String caseID = dis.readUTF();
                long docID = dis.readLong();
                if (action.equals("get")) {
                    writeDocument(res, getDocument(docID));
                } else if (action.equals("put")) {
                    result = String.valueOf(putDocument(new YDocument(caseID, docID, dis)));
                } else if (action.equals("remove")) {
                    result = String.valueOf(removeDocument(docID));
                } else if (action.equals("clearcase")) {
                    result = clearCase(caseID);
                } else if (action.equals("addcaseid")) {
                    result = addCaseID(docID, caseID);
                } else if (action.equals("completecase")) {
                    if (_retainWhenCaseCompletes) {
                        writeString(res,
                                "Documents not cleared: configured to retain on case completion",
                                "failure");
                    } else result = clearCase(caseID);
                }
            } else writeString(res, "Invalid or disconnected session handle", "failure");

            if (result != null) writeString(res, result, "response");
        } catch (EOFException eofe) {              // occurs when the inputStream is null
            writeString(res, "Welcome to the YAWL Document Store Service", "response");
        } catch (IOException ioe) {
            writeString(res, ioe.getMessage(), "failure");
        }
    }


    /**
     * Writes a binary file to a response's output stream
     *
     * @param res the response
     * @param doc a YDocument wrapper containing the binary file
     * @throws IOException if there's a problem writing to the stream
     */
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


    /**
     * Writes a UTF-8 String to a response's output stream
     *
     * @param res the response
     * @param msg the message to write
     * @param tag the xml tag to wrap the message in
     * @throws IOException if there's a problem writing to the stream
     */
    private void writeString(HttpServletResponse res, String msg, String tag)
            throws IOException {
        if (msg != null) {
            res.setContentType("text/xml; charset=UTF-8");
            OutputStreamWriter out = new OutputStreamWriter(res.getOutputStream(), "UTF-8");
            out.write(StringUtil.wrap(msg, tag));
            out.flush();
            out.close();
        }
    }


    /**
     * Reads a document from the database
     *
     * @param id the id of the document to read
     * @return a YDocument wrapper for the document
     * @throws IOException if no document can be found with the id passed
     */
    private YDocument getDocument(long id) throws IOException {
        try {
            return (YDocument) _db.load(YDocument.class, id);
        } catch (ObjectNotFoundException onfe) {
            throw new IOException("No stored document found with id: " + id);
        }
    }


    /**
     * Writes a document to the database
     *
     * @param doc a YDocument wrapper for the document to write
     * @return the id (primary key) of the stored document
     * @throws IOException if the document can't be read from the request stream
     */
    private long putDocument(YDocument doc) throws IOException {
        if (doc.getDocumentSize() > 0) {
            if (doc.hasValidId()) {

                // getDocument will propagate an exception if the id is unknown
                YDocument existingDoc = getDocument(doc.getId());
                existingDoc.setDocument(doc.getDocument());
                _db.exec(existingDoc, HibernateEngine.DB_UPDATE, true);
            } else {
                _db.exec(doc, HibernateEngine.DB_INSERT, true);
            }
            return doc.getId();
        } else throw new IOException("Could not read document from request stream");
    }


    /**
     * Removes a document from the database
     *
     * @param id the id of the document to remove
     * @return true if successful
     */
    private boolean removeDocument(long id) {
        try {
            YDocument doc = (YDocument) _db.load(YDocument.class, id);
            return (doc != null) && _db.exec(doc, HibernateEngine.DB_DELETE, true);
        } catch (ObjectNotFoundException onfe) {
            return false;
        }
    }


    private String addCaseID(long id, String caseID) throws IOException {
        try {
            YDocument doc = (YDocument) _db.load(YDocument.class, id);
            if (doc != null) {
                doc.setCaseId(caseID);
                if (_db.exec(doc, HibernateEngine.DB_UPDATE, true)) {
                    return "Case ID successfully updated";
                }
            }
            throw new IOException("No document found with id: " + id);
        } catch (ObjectNotFoundException onfe) {
            throw new IOException(onfe.getMessage());
        }
    }


    /**
     * Removes all the documents from the database that match the case id passed
     *
     * @param id the case id to remove documents for
     * @return a message indicating success or otherwise
     */
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
        } else {
            sb.append("Error removing documents for case: ").append(id);
        }
        return sb.toString();
    }


    /**
     * Increases the maximum column length for stored documents in H2 databases from 255
     * to 5Mb.
     * <p/>
     * H2 defaults to 255 chars for binary types, and this default went out
     * with the 2.3 release. This method (1) checks if we are using a H2 database, then
     * if so (2) checks the relevant column length, then if it is 255 (3) increases it
     * to 5Mb maximum and (4) writes a flag file so that the method short-circuits on
     * future executions (i.e. it only runs once).
     *
     * @param context the current servlet context
     */
    private void fixH2BinarySize(ServletContext context) {

        // if the flag files already exists, go no further
        if (context.getResourceAsStream("/WEB-INF/classes/dbfixed.bin") != null) {
            return;
        }

        // get the db properties from hibernate.properties
        Connection connection = null;
        Properties p = new Properties();
        try {
            p.load(context.getResourceAsStream("/WEB-INF/classes/hibernate.properties"));
            String dialect = p.getProperty("hibernate.dialect");

            // proceed only if this is a H2 database
            if (dialect != null && dialect.equals("org.hibernate.dialect.H2Dialect")) {
                Class.forName(p.getProperty("hibernate.connection.driver_class"));
                connection = DriverManager.getConnection(
                        p.getProperty("hibernate.connection.url"));
                if (connection != null) {

                    // get the YDOC column size, and increase if required
                    DatabaseMetaData dbmd = connection.getMetaData();
                    ResultSet rs = dbmd.getColumns(null, null, "YDOCUMENT", "YDOC");
                    rs.next();
                    if (rs.getInt("COLUMN_SIZE") <= 255) {
                        connection.createStatement().executeUpdate(
                                "ALTER TABLE ydocument ALTER COLUMN ydoc varbinary(5242880)");

                        // write flag file for next time
                        new File(context.getRealPath("WEB-INF/classes/dbfixed.bin")).createNewFile();
                    }
                    connection.close();
                }
            }
        } catch (Exception e) {
            // can't update
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException sqle) {
                //
            }
        }
    }


    private void deregisterDbDriver() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                Logger.getLogger(this.getClass()).info(
                        "Deregistered JDBC driver: " + driver);
            } catch (SQLException e) {
                Logger.getLogger(this.getClass()).warn(
                        "Unable to deregister JDBC driver " + driver, e);
            }
        }
    }

}
