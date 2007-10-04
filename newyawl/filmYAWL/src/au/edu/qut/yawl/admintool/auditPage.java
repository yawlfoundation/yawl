/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */



package au.edu.qut.yawl.admintool;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.edu.qut.yawl.engine.interfce.InterfaceA_EnvironmentBasedClient;

/**
 * @author heijens
 * @version 11.06.2005 added javadoc comments, changed queries because 
 * of removing spaces in tablenames and columns and removed code to test output.
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class auditPage extends HttpServlet{
	
	private InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://131.181.70.9:8080/yawl/ia");
	
	public void doGet(HttpServletRequest req, HttpServletResponse res){
		
	}
	
	
	public void doPost(HttpServletRequest req, HttpServletResponse res){	
		
		Connection connection = null;
		DBconnection.loadDriver("org.postgresql.Driver");
		
		boolean isClosed = DBconnection.getConnection();
		if(isClosed == true){
			DBconnection.getConnection();
		}
		
		DBconnection.printMetaData();
		
		String form = req.getParameter("which_form");
		
		String selecttable = req.getParameter("selecttable");
				
		//checks which button was clicked and invokes the corresponding method
		if(form.compareTo("GetAuditTrail")== 0){
			returnTableAttribute(selecttable, req, res);
		} else if(form.compareTo("addPosition")== 0) {
			//addPosition(positionID, positionname, orggroup, res);
		} else {
			System.out.println("Something went wrong, because no data is being added to the database. " +
					"Probably no values were entered into the fields");
			
		}
	}
	
	public void returnTableAttribute(String selecttable, HttpServletRequest req, HttpServletResponse res) {
		
		req.setAttribute("table", selecttable);
		
		RequestDispatcher requestDispatcher = req.getRequestDispatcher("/audit");
		try {
		requestDispatcher.forward(req, res);
		} catch (ServletException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	
}