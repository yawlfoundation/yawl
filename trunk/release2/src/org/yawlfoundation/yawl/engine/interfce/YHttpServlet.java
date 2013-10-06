package org.yawlfoundation.yawl.engine.interfce;

import org.apache.log4j.Logger;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.worklet.support.WorkletGateway;

import javax.servlet.http.HttpServlet;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

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
        _log = Logger.getLogger(this.getClass());
    }


    // should be called by any sub-class that uses hibernate or sets timers
    public void destroy() {
        deregisterDbDrivers();
        interruptTimerThreads();
    }


    protected boolean getInitBooleanValue(String param, boolean defValue) {
        return (param != null) ? param.equalsIgnoreCase("TRUE") : defValue;
    }


    protected String fail(String msg) {
        return StringUtil.wrap(msg, "failure");
    }


    protected String response(String result) {
        return StringUtil.wrap(result, "response");
    }


    private void deregisterDbDrivers() {
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                _log.info("Deregistered JDBC driver: " + driver);
            } catch (SQLException e) {
                _log.warn("Unable to deregister JDBC driver " + driver, e);
            }
        }
    }

    private void interruptTimerThreads() {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            String name = thread.getName();
            if (name != null && name.startsWith("Timer")) {
                thread.interrupt();
                _log.info("Interrupted running timer thread: " + name);
            }
        }
    }


}
