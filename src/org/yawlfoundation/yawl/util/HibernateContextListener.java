package org.yawlfoundation.yawl.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Michael Adams
 * @date 30/4/2026
 */
public class HibernateContextListener  implements ServletContextListener {

    private static final Logger _log = LogManager.getLogger(
            HibernateContextListener.class.getName());

    
    @Override
    public void contextInitialized(ServletContextEvent sce) {

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        _log.debug("Shutting down all background executors...");
        ShutdownTaskHandler.performShutdown();

        _log.debug("Closing all Hibernate Factories...");
        HibernateRegistry.shutdownAll();
    }
}