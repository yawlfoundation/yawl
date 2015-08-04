/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.elements.data.YVariable;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.util.XNode;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Author: Michael Adams
 * Creation Date: 8/07/2009
 */
public abstract class AbstractExternalDBGateway {

    protected Logger _log = Logger.getLogger(AbstractExternalDBGateway.class);


    // an instance of the backend 'engine' that provides the interface to the database
    protected HibernateEngine _dbEngine = HibernateEngine.getInstance();

    /**
     * Configures the engine to the database specified. Examples given are for postgres.
     * @param dialect the database dialect (e.g. "org.hibernate.dialect.PostgreSQLDialect")
     * @param driver the database driver (e.g. "org.postgresql.Driver")
     * @param url the database url (e.g. "jdbc:postgresql:yawl")
     * @param username the logon name for the database
     * @param password the logon password
     * @param classes a list of classes for Hibernate to use to converse with the
     * underlying tables (can be null if no classes are involved) 
     */
    protected void configureSession(String dialect, String driver, String url,
                                 String username, String password, List<Class> classes) {
        _dbEngine.configureSession(dialect, driver, url, username, password, classes);
    }


    /**
     * Configures the engine to the database specified in a properties file
     * @param propertiesFileName the full path & name of the properties file
     */
    protected void configureSession(String propertiesFileName, List<Class> classes) {
        if (propertiesFileName == null) return;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(propertiesFileName));
            _dbEngine.configureSession(props, classes);
        }
        catch (IOException e) {
            _log.error("Could not open properties file " + propertiesFileName, e);
        }
    }


    public String toXML() {
        XNode node = new XNode("ExternalDBGateway");
        node.addChild("name", getClass().getName());
        node.addChild("description", getDescription());
        return node.toString();
    }
    
    /********************************************************************************/
    /***** ABSTRACT METHODS *****/

    /**
     * @return a user-understandable description of the use of the particular
     * extending class, which will be seen in a list in the editor and from which the
     * user can choose the class.
     */
    public abstract String getDescription() ;


    /**
     * Populates the task parameter passed with a value selected from a database.
     * Called by the engine to populate a workitem when the workitem starts.
     * @param task the task template for the starting workitem.
     * @param param the name of the parameter that requires values.
     * @param caseData the current set of case variables and values.
     * @return an Element named after the param (use param.getName()), populated with 
     * the appropriate value.
     */
    public abstract Element populateTaskParameter(YTask task, YParameter param,
                                                  Element caseData);



    /**
     * Update the database with the workitem's values. Called by the engine when the
     * workitem completes.
     * @param paramName the name of the task of which this workitem is an instance.
     * @param outputData the datalist from which the corresponding database values are
     * to be updated.
     * @param caseData the current set of case variables and values.
     */
    public abstract void updateFromTaskCompletion(String paramName, Element outputData,
                                                  Element caseData);



    /**
     * Populates the case data template passed with values selected from a database.
     * Called by the engine to populate the case-level variables when the case starts.
     * Note that the the returned Element MUST be correctly populated with a value for
     * each input parameter. Local variables are passed so that their initial values
     * may (optionally) be changed, if required; to do so, change the value directly
     * within each variable - local variable values are NOT to be included in the
     * returned Element.
     * @param specID the specification identifier of the case.
     * @param caseID the case identifier.
     * @param inputParams the input parameters that require values.
     * @param localVars the local variables (optionally change their initial values).
     * @param caseDataTemplate the data structure that requires values.
     * @return an Element of the same structure as 'caseDataTemplate', with populated values.
     */
    public abstract Element populateCaseData(YSpecificationID specID, String caseID,
                                             List<YParameter> inputParams,
                                             List<YVariable> localVars,
                                             Element caseDataTemplate) ;


    /**
     * Update the database with the case's values. Called by the engine when the
     * case completes.
     * @param specID the specification identifier of the case.
     * @param caseID the case identifier.
     * @param outputParams the output parameters for the case.
     * @param updatingData the datalist from which the corresponding database values are
     * to be updated.
     */
    public abstract void updateFromCaseData(YSpecificationID specID,
                                            String caseID,
                                            List<YParameter> outputParams,
                                            Element updatingData) ;


}
