/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.LinkedList;
import au.edu.qut.yawl.authentication.User;

import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.exceptions.YAWLException;
import au.edu.qut.yawl.admintool.model.*;
import au.edu.qut.yawl.util.YMessagePrinter;
import net.sf.hibernate.*;

/**
 * @author fjellheim
 * @version 25.07.2005
 */
public class EditResourceAction extends HttpServlet {

    DatabaseGatewayImpl _model = null;

    boolean _persistanceConfiguredOn = false;

    private InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        ServletContext context = servletConfig.getServletContext();
        String persistOnStr = context.getInitParameter("EnablePersistance");
        _persistanceConfiguredOn = "true".equalsIgnoreCase(persistOnStr);
        if (_persistanceConfiguredOn) {
            try {
                _model = DatabaseGatewayImpl.getInstance(_persistanceConfiguredOn);
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res) {

    }


    public void doPost(HttpServletRequest req, HttpServletResponse res) {

        try {
            processPostQuery(req, res);

            getServletContext().getRequestDispatcher("/organizational.jsp?success=true").forward(req, res);

        } catch (Exception e) {
            try {
                ServletContext application = getServletContext();
                RequestDispatcher dispatcher =
                        application.getRequestDispatcher("/organizational.jsp?failure=" + e.getMessage());
                dispatcher.forward(req, res);
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

    }

    

    private void processPostQuery(HttpServletRequest req, HttpServletResponse res) throws Exception, YAuthenticationException {

        String action = req.getParameter("action");
        System.out.println("Action: " + action);

        if (("assignHuman2Roles".equalsIgnoreCase(action)) && (_persistanceConfiguredOn)) {
            assignHuman2Roles(req.getParameter("resourceRole"),
                    req.getParameter("selectrole"),
                    res);
	    
        } else if (("delResource".equalsIgnoreCase(action))) {
            delResource(req.getParameter("selectresource"),
			res);
        } else if ("addResource".equalsIgnoreCase(action)) {
            addResource(req.getParameter("resourceID"),
			req.getParameter("resdescription"),
			req.getParameter("type"),
			req.getParameter("worklist"), /* Not sure if this one exists */
			req.getParameter("givenname"),
			req.getParameter("surname"),
			req.getParameter("usertype"),
			req.getParameter("password"),
			req.getParameter("password2"),
			res);
        }

    }

    public void delResource(String resourceID, HttpServletResponse res) throws YPersistenceException, YAuthenticationException {
	
	if (_persistanceConfiguredOn) {
	    _model.deleteResource(resourceID);
	}
	
        //  Delete the user from the engine as well if the user is not already there
        String sessionHandle = new String();
        try {
            sessionHandle = iaClient.connect("admin", "YAWL");
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        String result = new String();
	boolean founduser = false;
        try {
	    List resultlist = new LinkedList();
	    resultlist = iaClient.getUsers(sessionHandle);
	    for (int i = 0; i < resultlist.size();i++) {
		User user = (User) resultlist.get(i);
		if (user.getUserID().equalsIgnoreCase(resourceID)) {
		    founduser = true;
		}
	    }
	    
	    System.out.println("deleting user: " + resourceID);
	    result = iaClient.deleteUser(resourceID,sessionHandle);
	    if(result.indexOf("<failure") != -1) {
		throw new YAuthenticationException(result);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	
    }


    public void addResource(String resourceID, String resdescription, String type, String worklist, String givenname, String surname, String usertype, String password, String password2, HttpServletResponse res) throws Exception, YAuthenticationException {

        Resource resource = null;
        if (! "Non-Human".equals(type)) {
            HumanResource hresource = new HumanResource(resourceID);
            hresource.setDescription(resdescription);
            hresource.setSurname(surname);
            hresource.setGivenName(givenname);
            hresource.setPasswords(password, password2);
            hresource.setIsAdministrator("Admin".equals(usertype));
            resource = hresource;
            List problems = hresource.verify();
            if (problems.size() > 0) {
                throw new YAWLException(YMessagePrinter.getMessageString(problems));
            }
        } else {
            NonHumanResource nhresource = new NonHumanResource(resourceID);
            nhresource.setDescription(resdescription);
            resource = nhresource;
        }
        // add the resource to the database
        if (_persistanceConfiguredOn) {
            try {
                _model.addOrEditResource(resource);
            } catch (YPersistenceException e) {
                throw e;
            }
        }


        //  Add the user to the engine as well if the user is not already there
        String sessionHandle = new String();
        try {
            sessionHandle = iaClient.connect("admin", "YAWL");
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        String result = new String();
	boolean founduser = false;
        try {
	    List resultlist = new LinkedList();
	    resultlist = iaClient.getUsers(sessionHandle);
	    for (int i = 0; i < resultlist.size();i++) {
		User user = (User) resultlist.get(i);
		if (user.getUserID().equalsIgnoreCase(resourceID)) {
		    founduser = true;
		}
	    }

            if (founduser) {
		result = iaClient.deleteUser(resourceID,sessionHandle);
		System.out.println("I found the user...." + result);
		if(result.indexOf("<failure") != -1) {
		    throw new YAuthenticationException(result);
		}
	    }
	    result = iaClient.createUser(resourceID, password, "Admin".equals(usertype), sessionHandle);
            if(result.indexOf("<failure") != -1) {
                throw new YAuthenticationException(result);
            }
        } catch (IOException e3) {
            e3.printStackTrace();
        }
    }
    

    public void assignHuman2Roles(String resourceID, String selectrole, HttpServletResponse res) throws YPersistenceException {
        //String url = getServletContext().getInitParameter("admintool") + "/addres.jsp";
        _model.assignHuman2Roles(resourceID, selectrole);
    }

}