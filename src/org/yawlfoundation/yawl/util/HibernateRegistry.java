package org.yawlfoundation.yawl.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Michael Adams
 * @date 30/4/2026
 */
public class HibernateRegistry {
    
    private static final Map<String, SessionFactory> factories = new ConcurrentHashMap<>();
    private static final Logger _log = LogManager.getLogger(HibernateRegistry.class);


    public static void registerFactory(String serviceName, SessionFactory factory) {
        factories.put(serviceName, factory);
    }


    public static SessionFactory getFactory(String serviceName) {
        return factories.get(serviceName);
    }


    public static void shutdownAll() {
        for (String serviceName : factories.keySet()) {
            _log.debug("Shutting down factory for " + serviceName);
            SessionFactory factory = factories.get(serviceName);
            if (factory != null && !factory.isClosed()) {
                factory.close();
            }
        }
    }


    public static String[] getServiceNames() {
        return factories.keySet().toArray(new String[0]);
    }

}

