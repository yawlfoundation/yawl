package org.yawlfoundation.yawl.engine.interfce;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yawlfoundation.yawl.util.StringUtil;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.jar.Manifest;

/**
 * An override of HttpServlet to include a few useful generic methods
 *
 * @author Michael Adams
 * @date 6/10/13
 */
public class YHttpServlet extends HttpServlet {

    protected static Logger _log;


    public YHttpServlet() {
        super();
        _log = LogManager.getLogger(this.getClass());
    }


    // should be called by any sub-class that uses hibernate or sets timers
    public void destroy() {
        deregisterDbDrivers();
        interruptTimerThreads();
    }


    protected boolean getBooleanFromContext(String param) {
        return getBooleanFromContext(param, false);
    }


    protected boolean getBooleanFromContext(String param, boolean defValue) {
        String s = getServletContext().getInitParameter(param);
        return s != null ? s.equalsIgnoreCase("true") : defValue;
    }


    protected String fail(String msg) {
        return StringUtil.wrap(msg, "failure");
    }


    protected String response(String result) {
        return StringUtil.wrap(result, "response");
    }


    protected Manifest getManifest() throws IOException {
        ServletContext application = getServletConfig().getServletContext();
        InputStream inputStream = application.getResourceAsStream("/META-INF/MANIFEST.MF");
        return new Manifest(inputStream);
    }


    private void deregisterDbDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                _log.info("Deregistered JDBC driver: {}", driver);
            } catch (SQLException e) {
                _log.warn("Unable to deregister JDBC driver {}: {}", driver, e.getMessage());
            }
        }
    }

    private void interruptTimerThreads() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            if (thread.getName().startsWith("Timer")) {
                thread.interrupt();
            }
        }
    }


}
