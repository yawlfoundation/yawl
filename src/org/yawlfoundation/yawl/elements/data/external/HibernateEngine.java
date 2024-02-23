/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
 * The YAWL Foundation is a collaboration of individuals and
 * organisations who are committed to improving workflow technology.
 *
 * This file is part of YAWL. YAWL is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Lesser
 * General Public License as published by the Free Software Foundation.
 *
 * YAWL is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with YAWL. If not, see <http://www.gnu.org/licenses/>.
 */

package org.yawlfoundation.yawl.elements.data.external;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.schema.TargetType;

import java.util.EnumSet;
import java.util.List;
import java.util.Properties;


/**
 *  This singleton class provides basic db support methods via Hibernate.
 *
 *  @author Michael Adams
 *  Date: 09/07/2009
 */

public class HibernateEngine {

    // persistence actions
    public static final int DB_UPDATE = 0;
    public static final int DB_DELETE = 1;
    public static final int DB_INSERT = 2;

    // reference to Hibernate session
    private static SessionFactory _factory = null;

    // instance reference
    private static HibernateEngine _me;

    private static final Logger _log = LogManager.getLogger(HibernateEngine.class);

    /*********************************************************************************/

    // Constructors and Initialisation //
    /***********************************/

    /** The constuctor - called from getInstance() */
    private HibernateEngine() throws HibernateException {
        initialise();
    }


    /** returns the current HibernateEngine instance */
    public static HibernateEngine getInstance() {
        if (_me == null) {
            try {
                _me = new HibernateEngine();
            }
            catch (HibernateException he) {
                _log.error("Could not initialise database connection.", he);
            }
        }
        return _me;
    }


    /** initialises hibernate and the required tables */
    public void initialise() throws HibernateException {
        _factory = new Configuration().buildSessionFactory();         // get a session context
    }


    public void configureSession(String dialect, String driver, String url,
                                 String username, String password, List<Class> classes)
            throws HibernateException {
        Properties props = new Properties();
        props.setProperty("hibernate.dialect", dialect);
        props.setProperty("hibernate.connection.driver_class", driver);
        props.setProperty("hibernate.connection.url", url);
        props.setProperty("hibernate.connection.username", username);
        props.setProperty("hibernate.connection.password", password);

        // add static props
        props.setProperty("hibernate.query.substitutions", "true 1, false 0, yes 'Y', no 'N'");
        props.setProperty("hibernate.show_sql", "false");
        props.setProperty("hibernate.current_session_context_class", "thread");
        props.setProperty("hibernate.jdbc.batch_size", "0");
        props.setProperty("hibernate.jdbc.use_streams_for_binary", "true");
        props.setProperty("hibernate.max_fetch_depth", "1");
        props.setProperty("hibernate.cache.region_prefix", "hibernate.test");
        props.setProperty("hibernate.cache.use_query_cache", "true");
        props.setProperty("hibernate.cache.use_second_level_cache", "true");
        props.setProperty("hibernate.cache.region.factory_class",
                          "org.hibernate.cache.ehcache.EhCacheRegionFactory");

        props.setProperty("hibernate.connection.provider_class",
                          "org.hibernate.connection.C3P0ConnectionProvider");
        props.setProperty("hibernate.c3p0.max_size", "20");
        props.setProperty("hibernate.c3p0.min_size", "2");
        props.setProperty("hibernate.c3p0.timeout", "5000");
        props.setProperty("hibernate.c3p0.max_statements", "100");
        props.setProperty("hibernate.c3p0.idle_test_period", "3000");
        props.setProperty("hibernate.c3p0.acquire_increment", "1");
        configureSession(props, classes);
    }


    public void configureSession(Properties props, List<Class> classes) {
        StandardServiceRegistryBuilder standardRegistryBuilder = new StandardServiceRegistryBuilder()
            .configure();
        if (props != null) {
            standardRegistryBuilder.applySettings(props);
        }
        StandardServiceRegistry standardRegistry = standardRegistryBuilder.build();

        MetadataSources metadataSources = new MetadataSources(standardRegistry);
        if (classes != null) {
            for (Class clazz : classes) {
                metadataSources.addClass(clazz);
            }
        }

        Metadata metadata = metadataSources.buildMetadata();
        _factory = metadata.buildSessionFactory();

        EnumSet<TargetType> targetTypes = EnumSet.of(TargetType.DATABASE);
        new SchemaUpdate().execute(targetTypes, metadata);
    }

    /******************************************************************************/

    /**
     * executes a Query object based on the sql string passed
     * @param queryString - the sql query to execute
     * @return the List of Object[]'s returned, or null if the query has some problem
     */
    public List execSQLQuery(String queryString) throws HibernateException {
        List result = null;
        Session session = _factory.getCurrentSession();
        session.beginTransaction();
        SQLQuery query = session.createSQLQuery(queryString);
        if (query != null) result = query.list();
        return result;
    }


    public List execQuery(String queryString) {
        List result = null;
        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = session.beginTransaction();
            Query query = session.createQuery(queryString);
            if (query != null) result = query.list();
        }
        catch (JDBCConnectionException jce) {
            _log.error("Caught Exception: Couldn't connect to datasource - " +
                    "starting with an empty dataset");
        }
        catch (HibernateException he) {
            _log.error("Caught Exception: Error executing query: " + queryString, he);
            if (tx != null) tx.rollback();
        }

        return result;
     }
    


    public List execNamedQuery(String namedQuery, String key) throws HibernateException {
        Session session = _factory.getCurrentSession();
        return session.getNamedQuery(namedQuery).setString("key", key).list();       
    }


    public int execUpdate(String queryString) throws HibernateException {
        int result = -1;
        Transaction tx = null;
        try {
            Session session = _factory.getCurrentSession();
            tx = session.beginTransaction();
            result = session.createQuery(queryString).executeUpdate();
        }
        catch (HibernateException he) {
            if (tx != null) tx.rollback();
            throw he;
        }

        return result;
     }


    /****************************************************************************/

}