/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.admintool;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;
import au.edu.qut.yawl.exceptions.YPersistenceException;
import au.edu.qut.yawl.admintool.model.*;
import net.sf.hibernate.*;

/**
* @author fjellheim
* @version 25.07.2005
*/
public class EditRoleAction extends HttpServlet{

    DatabaseGatewayImpl _model = null;


    private InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        String persistOnStr = context.getInitParameter("EnablePersistance");
        boolean _persistanceConfiguredOn = "true".equalsIgnoreCase(persistOnStr);
        if(_persistanceConfiguredOn) {
            try {
                _model = DatabaseGatewayImpl.getInstance(_persistanceConfiguredOn);
            } catch (HibernateException e) {
                e.printStackTrace();
            }
        }
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res){
	
    }
    
    
    public void doPost(HttpServletRequest req, HttpServletResponse res){
		
	
	try {
	    processPostQuery(req,res);

	    getServletContext().getRequestDispatcher("/roles.jsp?success=true").forward(req,res);

	} catch (Exception e) {
	    try {
		res.addHeader("failure","true");
		getServletContext().getRequestDispatcher("/roles.jsp?failure=true").forward(req,res);
		e.printStackTrace();
	    } catch (Exception e2) {
		e2.printStackTrace();
	    }
	}
	
    }

    private void processPostQuery(HttpServletRequest req, HttpServletResponse res)  throws YPersistenceException {
	try {
	    String action = req.getParameter("action");
	    System.out.println("Action: " + action);
	    
	    if ("assignRole2Humans".equals(action)) {
		assignRole2Humans(req.getParameter("role"),
				  req.getParameter("selecthuman"),
				  res);
		
	    } else if ("delRole".equals(action)) {
		delRole(req.getParameter("role"),
			res);
	    } else if ("addRole".equals(action)) {
		addRole(req.getParameter("addrole"),
			res);
		
	    }
	} catch (YPersistenceException e) {
	    throw e;
	}
	
		
    }

    public void delRole(String role, HttpServletResponse res) throws YPersistenceException {
	String url = getServletContext().getInitParameter("admintool")+"/organizational.jsp";
        try {
	    System.out.println("Deleting role: " + role);
            _model.deleteRole(role);
        } catch (YPersistenceException e) {
        }
    }
    
    
    public void addRole(String roleid, HttpServletResponse res)  throws YPersistenceException {
	
	/*
	  add the role to the database
	*/
	try {
	    _model.addRole(roleid);
	} catch (YPersistenceException e) {
	    /*
	      Failure
	    */
	}
	
	    
	    /*
	      Add the role to the engine as well???
	      
	      String sessionHandle = new String();
	      try {
	      sessionHandle = iaClient.connect("admin", "YAWL");
	      } catch (IOException e2) {
	      e2.printStackTrace();
	      }
	      String result = new String();
	      try {
	      result = iaClient.createUser(resourceID, password, "Admin".equals(usertype), sessionHandle);
	      } catch (IOException e3) {
	      e3.printStackTrace();
	      }
	    */

    }
    
    
    public void assignRole2Humans(String role, String resources, HttpServletResponse res) throws YPersistenceException {
        String url = getServletContext().getInitParameter("admintool")+"/addres.jsp";
        try {

	    System.out.println(role);
	    System.out.println(resources);

            _model.assignRole2Humans(role, resources);
        } catch (YPersistenceException e) {
            e.printStackTrace();
        }
    }
    
}