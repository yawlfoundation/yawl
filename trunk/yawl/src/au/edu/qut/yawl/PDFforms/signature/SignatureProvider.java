/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Group.  The YAWL Group is a collaboration of 
 * individuals and organiations who are commited to improving workflow technology.
 *
 */

package au.edu.qut.yawl.PDFforms.signature;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.ArrayList;

/**
 * This servlet generates digital IDs by running a batch file stored on the server's C:\genDir
 * @author Ignatius Ong
 */
public class SignatureProvider extends HttpServlet
{

	private HttpSession sess;
	private HttpServletRequest request;
	private HttpServletResponse response;

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws javax.servlet.ServletException, java.io.IOException
	{
		sess = request.getSession();
		this.request = request;
		this.response = response;

		String status = request.getParameter("status");

		if(status.equals("generate"))
			generateID();
	}

	private void generateID() throws javax.servlet.ServletException, java.io.IOException
	{
		ServletContext sc = getServletConfig().getServletContext();
		String path = sc.getRealPath("\\") + "secure";

		//retrieving the mandatory fields
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String alias = username;
		sess.setAttribute("alias", alias);
		try
		{
			//running batch file to generate digital ID
			String com = "cmd.exe /c start "  + "C:\\genDir\\CreateID.bat keystore.ks " + password + " " + alias + " C:\\genDir " + username + " " + username;

			Runtime rt = Runtime.getRuntime();
			Process p = rt.exec(com);

			//wait for batch process to end
			while (p.waitFor()!=0){}

			File privCert = new File("C:\\genDir\\certificate.pfx");
			File pubCert = new File("C:\\genDir\\pubcertfile.cer");

			//move generated files to secure directory
			privCert.renameTo(new File(path+"\\genDir\\certificate.pfx"));
			pubCert.renameTo(new File(path+"\\certificates\\"+alias+".cer"));

			//delete files after generated
			privCert.delete();
			pubCert.delete();


			//update ID store
			//File dbfile = new File(path+"\\db\\store.dat");
			//FileInputStream fis = new FileInputStream(dbfile);
			//ObjectInputStream ois = new ObjectInputStream(fis);
			

			//ArrayList list = (ArrayList) ois.readObject();
			//ois.close();

			//list.add(firstName + " " + lastName);

			//FileOutputStream fos = new FileOutputStream(path+"\\db\\store.dat");
			//ObjectOutputStream oos = new ObjectOutputStream(fos);

			//oos.writeObject(list);
			//oos.close();
		}
		catch(Exception e){e.printStackTrace();}

		response.sendRedirect("./finish.jsp");
	}
}
