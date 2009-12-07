package org.yawlfoundation.yawl.elements.data.external;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;

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
     * @param task the task template for the starting workitem
     * @param param the name of the parameter that requires values
     * @param caseData the current set of case variables and values
     * @return an Element named after the param (use param.getName()), populated with 
     * the appropriate value
     */
    public abstract Element populateTaskParameter(YTask task, YParameter param,
                                                  Element caseData);



    /**
     * Update the database with the workitem's values. Called by the engine when the
     * workitem completes.
     * @param paramName the name of the task of which this workitem is an instance
     * @param outputData the datalist from which the corresponding database values are
     * to be updated
     * @param caseData the current set of case variables and values
     */

    public abstract void updateFromTaskCompletion(String paramName, Element outputData, Element caseData);



    /**
     * Populates the case data template passed with values selected from a database.
     * Called by the engine to populate the case-level variables when the case starts.
     * @param specID the specification identifier of the case
     * @param caseDataTemplate the data structure that requires values
     * @return an Element of the same structure as 'caseDataTemplate', with populated values
     */
    public abstract Element populateCaseData(YSpecificationID specID, Element caseDataTemplate) ;


    /**
     * Update the database with the case's values. Called by the engine when the
     * case completes.
     * @param specID the specification identifier of the case
     * @param updatingData the datalist from which the corresponding database values are
     * to be updated
     */
    public abstract void updateFromCaseData(YSpecificationID specID, Element updatingData) ;


}
