package org.yawlfoundation.yawl.reporter;

import org.yawlfoundation.yawl.engine.interfce.YHttpServlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

/**
 * @author Michael Adams
 * @date 1/10/15
 */
public class Reporter extends YHttpServlet {


    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        ServletContext context = getServletContext();
        String result = "<error>Malformed or missing report parameter</error>";

        try {
            String reportStr = req.getParameter("report");
            if (reportStr != null) {
                Report r = new Report();
                r.fromXML(reportStr);
                InputStream is = context.getResourceAsStream(
                        "/WEB-INF/classes/sender.properties");
                 result = new Sender(is).send(r.getSubject(), r.getHTML());
            }
        }
        catch (Exception e) {
            result = "<error>" + e.getMessage() + "</error>";
        }
        res.setContentType("text/xml; charset=UTF-8");
        PrintWriter out = res.getWriter();
        out.write(result);
        out.flush();
        out.close();
    }

}
