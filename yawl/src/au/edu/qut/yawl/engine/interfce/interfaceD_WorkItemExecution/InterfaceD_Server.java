/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce.interfaceD_WorkItemExecution;

import au.edu.qut.yawl.worklist.model.WorkItemRecord;
import au.edu.qut.yawl.worklist.model.Marshaller;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Receives workitem events from an interface client.
 *
 * 
 * @author Lachlan Aldred
 * Date: 16/09/2005
 * Time: 15:48:08
 */
public class InterfaceD_Server extends HttpServlet{
    private InterfaceD_Controller _controller;

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext context = servletConfig.getServletContext();
        String controllerClassName =
                context.getInitParameter("InterfaceBWebSideController");
        //If you need to get through an auth proxy firewall and have configured it in the
        //web.xml file they will be retrieved for use.
        try {
            Class controllerClass = Class.forName(controllerClassName);
            _controller = (InterfaceD_Controller) controllerClass.newInstance();
            //here the URL of the YAWL Engine get retrieved from the web.xml file.
            context.setAttribute("controller", _controller);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {

    }



    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        String workitemStr = request.getParameter("workitem");
        WorkItemRecord workitem = Marshaller.unmarshalWorkItem(workitemStr);
        _controller.processWorkItem(workitem);
    }

}
