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
import java.sql.ResultSet;
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
* @version 25.07.2005
*/
public class EditServlet extends HttpServlet{

	private InterfaceA_EnvironmentBasedClient iaClient = new InterfaceA_EnvironmentBasedClient("http://localhost:8080/yawl/ia");

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

		String role = req.getParameter("role");
		String role2role = req.getParameter("role2role");
		String role2pos = req.getParameter("role2pos");
		String cap = req.getParameter("cap");
		String selectpos = req.getParameter("selectpos");
		String position = req.getParameter("position");
		String positionname = req.getParameter("positionname");
		String orggroup = req.getParameter("orggroup");
		String submittype = "";
		String selectresource = req.getParameter("selectres");
		String resourceID = req.getParameter("resourceID");
		String resdescription = req.getParameter("resdescription");
		String type = req.getParameter("type");
		String worklist = req.getParameter("worklist");
		String givenname = req.getParameter("givenname");
		String surname = req.getParameter("surname");
		String usertype = req.getParameter("usertype");
		String password = req.getParameter("password");
		String password2 = req.getParameter("password2");
		String selectrole = req.getParameter("selectrole");
		String selectposition = req.getParameter("selectposition");
		String selectcapability = req.getParameter("selectcapability");
        String subid = req.getParameter("subid");
        String subfrom = req.getParameter("subfrom");
        String subto = req.getParameter("subto");
        String subid2 = req.getParameter("subid2");

		if (req.getParameter("editpos") != null) {
			submittype = "editpos";
		}
		if (req.getParameter("deletepos") !=null) {
			submittype = "deletepos";
		}
		if (req.getParameter("updatepos") !=null) {
			submittype = "updatepos";
		}

		String selectres = req.getParameter("selectres");

		if (req.getParameter("editres") != null) {
			submittype = "editres";
		}
		if (req.getParameter("deleteres") !=null) {
			submittype = "deleteres";
		}
		if (req.getParameter("updateres") !=null) {
			submittype = "updateres";
		}

        String selectsub = req.getParameter("selectsub");

		if (req.getParameter("editsub") != null) {
			submittype = "editsub";
		}
		if (req.getParameter("deletesub") !=null) {
			submittype = "deletesub";
		}
		if (req.getParameter("updatesub") !=null) {
			submittype = "updatesub";
		}

		//checks which button was clicked and invokes the corresponding method
		if(form.compareTo("deleteroles")== 0){
			deleteRole(role, res);
		} else if(form.compareTo("deleteconnectedroles")== 0) {
			deleteConnectedRoles(role2role, res);
		} else if(form.compareTo("deleterolepos")==0) {
			deleteRolePosition(role2pos, res);
		} else if (form.compareTo("deletecap")==0) {
			deleteCap(cap, res);
		} else if ((form.compareTo("selectposition")==0 && submittype.compareTo("editpos")==0)) {
				selectPosition(selectpos, res, req);
		} else if ((form.compareTo("selectposition")==0 && submittype.compareTo("deletepos")==0)) {
				deletePosition(selectpos, res);
		} else if (submittype.compareTo("updatepos")==0) {
				editPosition(position, positionname, orggroup, res);
		} else if ((form.compareTo("selectresource")==0 && submittype.compareTo("editres")==0)) {
				selectResource(selectresource, res, req);
		} else if ((form.compareTo("selectresource")==0 && submittype.compareTo("deleteres")==0)) {
				deleteResource(selectresource, res);
		} else if (submittype.compareTo("updateres")==0) {
				editResource(resourceID, resdescription, type, worklist, givenname, surname, usertype, password, password2, res);
        } else if ((form.compareTo("selectsubstitution")==0 && submittype.compareTo("editsub")==0)) {
				selectSubstitution(selectsub, res, req);
		} else if ((form.compareTo("selectsubstitution")==0 && submittype.compareTo("deletesub")==0)) {
				deleteSubstitution(selectsub, res);
		} else if (submittype.compareTo("updateres")==0) {
				editSubstitution(subid, subfrom, subto, subid2, res);
		} else if(form.compareTo("deleteResourceCapability")==0){
				delResCap(resourceID, selectcapability, res);
		} else if(form.compareTo("deleteResourcePosition")==0){
				delHResPos(resourceID, selectposition, res);
		} else if(form.compareTo("deleteResourceRole")==0){
				delHResRole(resourceID, selectrole, res);
		} else {
			System.out.println("Something went wrong, no data is being added to the database. " +
					"Probably no values were entered into the fields");
		}
	}

	/**
	 * Deletes the role that the user selected
	 * @param role the role that needs to be deleted
	 * @param res
	 */
	public void deleteRole(String role, HttpServletResponse res){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String sql = "DELETE FROM Role WHERE RoleName LIKE ('"+role+"');";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
					"Check whether the values entered are already added to the _model");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editrole.jsp?failure=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editrole.jsp?success=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}


	/**
	 * @param role2role
	 * @param res
	 */
	public void deleteConnectedRoles(String role2role, HttpServletResponse res){
		int nrows = 0;
		String role1 = "";
		String role2 = "";
		for (int i=0; i <role2role.length(); i++) {
				role1 = role2role.substring(0, role2role.indexOf('-'));
				role2 = role2role.substring((role2role.indexOf('-')+1), role2role.length());
			}
		System.out.println("role1 =" + role1);
		System.out.println("role2 =" + role2);
		Statement statement = DBconnection.createStatement();
		String sql = "DELETE FROM RoleIncorporatesRole WHERE RoleName LIKE ('"+role1+"') AND IncorporatesRole LIKE ('"+role2+"');";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
					"Check whether the values entered are already added to the _model");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editrole.jsp?failure=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editrole.jsp?success=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}


	/**
	 * @param role2pos
	 * @param res
	 */
	public void deleteRolePosition(String role2pos, HttpServletResponse res){
		int nrows = 0;
		String role = "";
		String pos = "";
		for (int i=0; i <role2pos.length(); i++) {
				role = role2pos.substring(0, role2pos.indexOf('-'));
				pos = role2pos.substring((role2pos.indexOf('-')+1), role2pos.length());
		}
		System.out.println("role =" + role);
		System.out.println("pos =" + pos);
		Statement statement = DBconnection.createStatement();
		String sql = "DELETE FROM RolePosition WHERE RoleName LIKE ('"+role+"') AND PositionID LIKE ('"+pos+"');";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
					"Check whether the values entered are already added to the _model");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editrole.jsp?failure=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editrole.jsp?success=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Deletes the capability that the user has selected
	 * @param cap
	 * @param res
	 */
	public void deleteCap(String cap, HttpServletResponse res){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String sql = "DELETE FROM Capability WHERE Capabilitydesc LIKE ('"+cap+"');";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
					"Check whether the values entered are already added to the _model");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editcap.jsp?failure=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editcap.jsp?success=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * Gets the requested positioninformation from the database and returns this information to editpos.jsp
	 * @param selectpos
	 * @param res
	 * @param req
	 */
	public void selectPosition(String selectpos, HttpServletResponse res, HttpServletRequest req){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String posID = selectpos.substring(0, selectpos.indexOf('-'));
		String sql = "SELECT * FROM Position WHERE PositionID LIKE ('"+posID+"');";
		ResultSet position = DBconnection.getResultSet(statement, sql);

		String positionID = "";
		String positionname= "";
		String orggroup = "";
		try {
			while(position.next()){
			positionID = position.getString(1);
			positionname = position.getString(2);
			orggroup = position.getString(3);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		req.setAttribute("positionID", positionID);
		req.setAttribute("positionname", positionname);
		req.setAttribute("orggroup", orggroup);

		RequestDispatcher requestDispatcher = req.getRequestDispatcher("/editpos");
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

	public void editPosition(String position, String positionname, String orggroup, HttpServletResponse res){
		int nrows = 0;
		int totalrows = 0;
		Statement statement = DBconnection.createStatement();
		String sql = "UPDATE Position SET PositionName = '"+positionname+"', BelongsToOrgGroup = '"+orggroup+"' WHERE PositionID = '"+position+"';";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Not the correct number of rows changed.");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editpos.jsp?failureedit=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editpos.jsp?successedit=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void deletePosition(String position, HttpServletResponse res){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String posID = position.substring(0, position.indexOf('-'));
		System.out.println("posID: "+posID);
		String sql = "DELETE FROM Position WHERE PositionID LIKE ('"+posID+"');";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
					"Check whether the values entered are already added to the _model");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editpos.jsp?failuredelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editpos.jsp?successdelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void selectResource(String selectres, HttpServletResponse res, HttpServletRequest req){
		int nrows = 0;
		Statement statement1 = DBconnection.createStatement();
		String resourceID = selectres.substring(0, selectres.indexOf('-'));
		String sql1 = "SELECT * FROM ResSerPosID WHERE ID LIKE ('"+resourceID+"');";
		ResultSet resource = DBconnection.getResultSet(statement1, sql1);

		String resID = "";
		String resourcetype= "";
		String resdescription = "";
		String worklist = "";
		String surname = "";
		String givenname = "";
		String password = "";
		try {
			while(resource.next()){
			resID = resource.getString(1);
			resourcetype = resource.getString(2);
			resdescription = resource.getString(4);
			worklist = resource.getString(5);
			surname = resource.getString(6);
			givenname = resource.getString(7);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		req.setAttribute("resourceID", resID);
		req.setAttribute("resourcetype", resourcetype);
		req.setAttribute("resdescription", resdescription);
		req.setAttribute("worklist", worklist);
		req.setAttribute("surname", surname);
		req.setAttribute("givenname", givenname);
		req.setAttribute("password", password);

		RequestDispatcher requestDispatcher = req.getRequestDispatcher("/editres");
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

	public void editResource(String resourceID, String resdescription, String type, String worklist, String givenname, String surname, String usertype, String password, String password2, HttpServletResponse res){
		int nrows = 0;
		String ResSerPosType = "Resource";
		if (type.compareTo("Human")== 0){
			if ((password.compareTo("")==0) || (password.compareTo(password2)!=0) ) {
				String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?forgotpassword=true");
				try {
					res.sendRedirect(res.encodeURL(url));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else {
				Statement statement = DBconnection.createStatement();
				String sql = "UPDATE ResSerPosID SET IsOfResSerPosType = '"+ResSerPosType+"', IsOfResourceType = '"+type+"', ResourceDescription = '"+resdescription+"', UsesWorklist = '"+worklist+"', SurName = '"+surname+"', GivenName = '"+givenname+"', Password = '"+password+"' WHERE ID = '"+resourceID+"';";
				try {
					nrows = statement.executeUpdate(sql);
					statement.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (nrows != 1) {
					System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
						"Check whether the values entered are already added to the _model");
					String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?failureedit=true");
						try {
							res.sendRedirect(res.encodeURL(url));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				}
				/*else {
					String sessionHandle = new String();
					try {
						sessionHandle = iaClient.connect("admin", "YAWL");
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					System.out.println("sessionHandle: "+sessionHandle);
					String result = new String();
					try {
						if(usertype.compareTo("Admin")==0){
							result = iaClient.createUser(resourceID, password, true, sessionHandle);
						}
						else { //usertype = normal user
							result = iaClient.createUser(resourceID, password, false, sessionHandle);
						}
					} catch (IOException e3) {
						// TODO Auto-generated catch block
						e3.printStackTrace();
					}
					String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?success=true");
						try {
							res.sendRedirect(res.encodeURL(url));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}*/
			}
		}
		else {
			if(type.compareTo("Non-Human")==0) {
				Statement statement = DBconnection.createStatement();
				//todo Lachlan: this is not valid SQL;
                String sql = "UPDATE ResSerPosID SET IsOfResSerPosType = '"+ResSerPosType+"', IsOfResourceType = '"+type+"', ResourceDescription = '"+resdescription+"' WHERE'"+resourceID+"', '"+ResSerPosType+"', '"+type+"', '"+resdescription+"', 'null', 'null', 'null', 'null', 'null', 'null', 'null');";
			try {
				nrows = statement.executeUpdate(sql);
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (nrows != 1) {
				System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
						"Check whether the values entered are already added to the _model");
				String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?failureedit=true");
				try {
					res.sendRedirect(res.encodeURL(url));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else {
				String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?successedit=true");
				try {
					res.sendRedirect(res.encodeURL(url));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			}
		}
	}


	public void deleteResource(String resourceID, HttpServletResponse res){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String resID = resourceID.substring(0, resourceID.indexOf('-'));
		String sql = "DELETE FROM ResSerPosID WHERE ID LIKE ('"+resID+"');";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
					"Check whether the values entered are already added to the _model");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?failuredelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?successdelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void delResCap(String resourceID, String selectcapability, HttpServletResponse res){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String sql = "DELETE FROM ResourceCapability WHERE ResourceID = ('"+resourceID+"') AND Capabilitydesc = ('"+selectcapability+"';";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. ");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?failuredelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?successdelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void delHResPos(String resourceID, String selectposition, HttpServletResponse res){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String sql = "DELETE FROM HResOccupiesPos WHERE HResID = ('"+resourceID+"') AND PositionID = ('"+selectposition+"';";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. ");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?failuredelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?successdelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	public void delHResRole(String resourceID, String selectrole, HttpServletResponse res){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String sql = "DELETE FROM HResPerformsRole WHERE HResID = ('"+resourceID+"') AND RoleName = ('"+selectrole+"';";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. ");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?failuredelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editres.jsp?successdelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

    public void selectSubstitution(String selectsub, HttpServletResponse res, HttpServletRequest req){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String resID = selectsub.substring(0, selectsub.indexOf('-'));
		String sql = "SELECT * FROM ResPosSubstitution WHERE ResPosID LIKE ('"+resID+"');";
		ResultSet substitution = DBconnection.getResultSet(statement, sql);

		String resposID = "";
		String subfrom= "";
		String subto = "";
        String substi= "";

		try {
			while(substitution.next()){
			resposID = substitution.getString(1);
			subfrom = substitution.getString(2);
			subto = substitution.getString(3);
            substi = substitution.getString(4);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		req.setAttribute("resposID", resposID);
		req.setAttribute("subfrom", subfrom);
		req.setAttribute("subto", subto);
        req.setAttribute("substi", substi);

		RequestDispatcher requestDispatcher = req.getRequestDispatcher("/editsub");
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

    public void deleteSubstitution(String selectsub, HttpServletResponse res){
		int nrows = 0;
		Statement statement = DBconnection.createStatement();
		String subID = selectsub.substring(0, selectsub.indexOf('-'));
		String sql = "DELETE FROM ResPosSubstitution WHERE ResPosID LIKE ('"+subID+"');";
		try {
			nrows = statement.executeUpdate(sql);
			statement.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (nrows != 1) {
			System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
					"Check whether the values entered are already added to the _model");
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editsub.jsp?failuredelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		else {
			String url =  new String(getServletContext().getInitParameter("admintool")+"/editsub.jsp?successdelete=true");
			try {
				res.sendRedirect(res.encodeURL(url));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

    public void editSubstitution(String subid1, String subfrom, String subto, String subid2, HttpServletResponse res){
        if (subid1.compareTo(subid2)==0){
            String url = new String(getServletContext().getInitParameter("admintool")+"/editsub.jsp?same=true");
            try {
                res.sendRedirect(res.encodeURL(url));
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        } else if ((subfrom.charAt(2) != '-' && subfrom.charAt(5) != '-') || (subto.charAt(2) != '-' && subto.charAt(5) != '-')) {
            System.out.println("wrongdate format");
            String url =  new String(getServletContext().getInitParameter("admintool")+"/editsub.jsp?wrongdate=true");
            try {
                res.sendRedirect(res.encodeURL(url));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } else {
            int nrows = 0;

//		 convert dates to right format for database
            String fromyear  = subfrom.substring(6,10);
            String frommonth  = subfrom.substring(3,5);
            String fromday  = subfrom.substring(0,2);

            String fromdate = (fromyear+"-"+frommonth+"-"+fromday);

            String untilyear  = subto.substring(6,10);
            String untilmonth  = subto.substring(3,5);
            String untilday  = subto.substring(0,2);

            String untildate = (untilyear+"-"+untilmonth+"-"+untilday);

            Statement statement = DBconnection.createStatement();
            String sql = "UPDATE ResPosSubstitution SET from = '"+fromdate+"', to = '"+untildate+"', HasSubstitute =  '"+subid2+"' WHERE ResPosID = '"+subid1+"');";
            try {
                nrows = statement.executeUpdate(sql);
                statement.close();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (nrows != 1) {
            System.out.println("Instead of 1 row, " +nrows+ " were affected. " +
                    "Check whether the values entered are already added to the _model");
            String url =  new String(getServletContext().getInitParameter("admintool")+"/editsub.jsp?failureedit=true");
            try {
                res.sendRedirect(res.encodeURL(url));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
        else {
            String url =  new String(getServletContext().getInitParameter("admintool")+"/editsub.jsp?successedit=true");
            try {
                res.sendRedirect(res.encodeURL(url));
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }
}


}
