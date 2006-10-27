/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import au.edu.qut.yawl.exceptions.YPersistenceException;


/**
 * 
 * @author Lachlan Aldred
 * Date: 22/12/2003
 * Time: 12:03:41
 * 
 */
public class InterfaceA_EngineBasedServer extends HttpServlet {
    private EngineGateway _engine;
    private static final boolean _debug = false;
    private static Logger logger = null;

    public void init() throws ServletException {

        /**
         * Initialise logging
         */
        logger = Logger.getLogger(this.getClass());
        
        ServletContext context = getServletContext();

        /*
  ADDED FOR PERSISTANCE TO CHECK IF
  DATABASE IS ENABLED/DISABLED
*/
        try {
            String persistOn = context.getInitParameter("EnablePersistance") ;
            boolean enablePersist = "true".equalsIgnoreCase(persistOn);

            _engine = (EngineGateway) context.getAttribute("engine");
            if (_engine == null) {
                _engine = new EngineGatewayImpl(enablePersist);
                context.setAttribute("engine", _engine);
            }
        } catch (YPersistenceException e) {
            logger.fatal("Failure to initialise runtime (persistence failure)", e);
            throw new UnavailableException("Persistence failure");
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //reloading of the remote engine.

        PrintWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        output.append(processGetQuery(request));
        output.append("</response>");
        if (_engine.enginePersistenceFailure())
        {
            logger.fatal("************************************************************");
            logger.fatal("A failure has occured whilst persisting workflow state to the");
            logger.fatal("database. Check the satus of the database connection defined");
            logger.fatal("for the YAWL service, and restart the YAWL web application.");
            logger.fatal("Further information may be found within the Tomcat log files.");
            logger.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }
        ServletUtils.finalizeResponse(outputWriter, output);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //reloading of the remote engine.

        PrintWriter outputWriter = ServletUtils.prepareResponse(response);
        StringBuffer output = new StringBuffer();
        output.append("<response>");
        output.append(processPostQuery(request));
        output.append("</response>");
        if (_engine.enginePersistenceFailure())
        {
            logger.fatal("************************************************************");
            logger.fatal("A failure has occured whilst persisting workflow state to the");
            logger.fatal("database. Check the satus of the database connection defined");
            logger.fatal("for the YAWL service, and restart the YAWL web application.");
            logger.fatal("Further information may be found within the Tomcat log files.");
            logger.fatal("************************************************************");
            response.sendError(500, "Database persistence failure detected");
        }
        ServletUtils.finalizeResponse(outputWriter, output);
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) {

    }


    protected void doDelete(HttpServletRequest request, HttpServletResponse responce) {

    }


    //###############################################################################
    //      Start YAWL Processing methods
    //###############################################################################
    private String processGetQuery(HttpServletRequest request) {
        StringBuffer msg = new StringBuffer();
        try {
            if (_debug) {
                System.out.println("\nInterfaceA_EngineBasedServer:doGet() request.getRequestURL = "
                        + request.getRequestURL());
                System.out.println("InterfaceA_EngineBasedServer::doGet() request.parameters:");
                Enumeration paramNms = request.getParameterNames();
                while (paramNms.hasMoreElements()) {
                    String name = (String) paramNms.nextElement();
                    System.out.println("\trequest.getParameter(" + name + ") = "
                            + request.getParameter(name));
                }
            }
            //if parameters exist do the while
            String sessionHandle = request.getParameter("sessionHandle");
            String action = request.getParameter("action");
            if (action != null) {
                if (action.equals("checkConnection")) {
                    msg.append(_engine.checkConnectionForAdmin(sessionHandle));
                } else if (action.equals("getUsers")) {
                    msg.append(_engine.getUsers(sessionHandle));
                } else if (action.equals("getList")) {
                    msg.append(_engine.getSpecificationList(sessionHandle));
                } else if (action.equals("getYAWLServices")) {
                    msg.append(_engine.getYAWLServices(sessionHandle));
                }
            }
            String specid = request.getParameter("specID");
            if (specid != null) {
                msg.append(_engine.getProcessDefinition(specid, sessionHandle));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (msg.length() == 0) {
            msg.append(
                    "<failure><reason>" +
                    "params invalid or execption was thrown." +
                    "</reason></failure>");
        }
        if (_debug) {
            System.out.println("return = " + msg);
        }
        return msg.toString();
    }


    private String processPostQuery(HttpServletRequest request) {
        StringBuffer msg = new StringBuffer();
        try {
            if (_debug) {
                System.out.println("\nInterfaceA_EngineBasedServer::doPost() request.getRequestURL = "
                        + request.getRequestURL());
                System.out.println("InterfaceA_EngineBasedServer::doPost() request.parameters:");
                Enumeration paramNms = request.getParameterNames();
                while (paramNms.hasMoreElements()) {
                    String name = (String) paramNms.nextElement();
                    System.out.println("\trequest.getParameter(" + name + ") = "
                            + request.getParameter(name));
                }
            }
            //if parameters exist do the while
            String sessionHandle = request.getParameter("sessionHandle");
            String action = request.getParameter("action");
            if (action != null) {
            	if ("connect".equals(action)) {
            		String userID = request.getParameter("userid");
                    String password = request.getParameter("password");
                    msg.append(_engine.connect(userID, password));
            	}
            	else if ("createUser".equals(action) || "createAdmin".equals(action)) {
                    String userName = request.getParameter("userName");
                    String password = request.getParameter("password");
                    boolean isAdmin;
                    if ("createAdmin".equals(action)) {
                        isAdmin = true;
                    } else {
                        isAdmin = false;
                    }
                    msg.append(_engine.createUser(userName, password, isAdmin, sessionHandle));
                } else if ("deleteUser".equals(action)) {
                    String userName = request.getParameter("userName");
                    msg.append(_engine.deleteUser(userName, sessionHandle));
                } else if ("newPassword".equals(action)) {
                    String password = request.getParameter("password");
                    msg.append(_engine.changePassword(password, sessionHandle));
                } else if ("newYAWLService".equals(action)) {
                    String serviceStr = request.getParameter("service");
                    msg.append(_engine.addYAWLService(serviceStr, sessionHandle));
                } else if ("removeYAWLService".equals(action)) {
                    String serviceURI = request.getParameter("serviceURI");
                    msg.append(_engine.removeYAWLService(serviceURI, sessionHandle));
                } else if ("unload".equals(action)) {
                	String specID = request.getParameter( "specID" );
                	msg.append( _engine.unloadSpecification( specID, sessionHandle ) );
                } else if ("upload".equals(action)) {
                    String fileName = request.getParameter("filename");
                    String specification = request.getParameter( "specification" );
                    msg.append(_engine.loadSpecification(
                            specification.toString(),
                            fileName,
                            sessionHandle));
                } else {
                	msg.append( "<failure><reason>unrecognized action:" + action + "</reason></failure>" );
                }
            } else {
            	System.out.println("\nInterfaceA_EngineBasedServer::doPost() request.getRequestURL = "
                        + request.getRequestURL());
                System.out.println("InterfaceA_EngineBasedServer::doPost() request.parameters:");
                Enumeration paramNms = request.getParameterNames();
                while (paramNms.hasMoreElements()) {
                    String name = (String) paramNms.nextElement();
                    System.out.println("\trequest.getParameter(" + name + ") = "
                            + request.getParameter(name));
                }
            	msg.append( "<failure><reason>No action was specified</reason></failure>" );
            }
        } catch (Exception e) {
        	StringWriter sw = new StringWriter();
            e.printStackTrace( new PrintWriter( sw ) );
            try {
				msg.append(
						"<failure><reason>" +
						URLEncoder.encode( sw.toString(), "UTF-8" ) +
						"</reason></failure>" );
			}
			catch( UnsupportedEncodingException e1 ) {
				// shouldn't ever happen
				throw new Error( e );
			}
        }
        if (msg.length() == 0) {
            msg.append(
                    "<failure><reason>" +
                    "params invalid or exception was thrown." +
                    "</reason></failure>");
        }
        if (_debug) {
            System.out.println("return = " + msg);
        }
        return msg.toString();
    }
}


