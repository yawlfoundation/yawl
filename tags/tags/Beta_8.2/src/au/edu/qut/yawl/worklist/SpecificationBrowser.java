/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.worklist;

import au.edu.qut.yawl.worklist.model.SpecificationData;
import au.edu.qut.yawl.worklist.model.WorklistController;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 8/03/2004
 * Time: 14:41:54
 * 
 */
public class SpecificationBrowser extends HttpServlet {


    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WorklistController controller = null;
        ServletContext context = getServletContext();
        controller = (WorklistController) context.getAttribute(
                "au.edu.qut.yawl.worklist.model.WorklistController");
        if (controller == null) {
            controller = new WorklistController();
            controller.setUpInterfaceBClient(context.getInitParameter("InterfaceB_BackEnd"));
            controller.setUpInterfaceAClient(context.getInitParameter("InterfaceA_BackEnd"));
            context.setAttribute("au.edu.qut.yawl.worklist.model.WorklistController", controller);
        }
        response.setContentType("text/xml");
        StringBuffer output = new StringBuffer();
        PrintWriter outputWriter = response.getWriter();
        String specID = request.getParameter("specID");
        if (specID != null) {
            SpecificationData specData = controller.getSpecificationData(
                    specID,
                    (String) request.getSession().getAttribute("sessionHandle"));
            String specAsXML = specData.getAsXML();
            output.append(specAsXML);
        }
        outputWriter.write(output.toString());
        outputWriter.flush();
        outputWriter.close();
    }
}
