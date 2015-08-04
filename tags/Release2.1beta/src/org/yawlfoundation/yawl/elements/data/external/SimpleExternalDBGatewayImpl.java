package org.yawlfoundation.yawl.elements.data.external;

import org.jdom.Element;
import org.yawlfoundation.yawl.elements.YTask;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.YSpecificationID;

/**
 * Author: Michael Adams
 * Creation Date: 13/08/2009
 */
public class SimpleExternalDBGatewayImpl extends AbstractExternalDBGateway {

    public SimpleExternalDBGatewayImpl() {}

    private boolean configured = false;

    public void updateFromCaseData(YSpecificationID specID, Element updatingData) { }

    public Element populateTaskParameter(YTask task, YParameter param, Element caseData) {
        Element result = new Element(param.getName());
//        if (configure()) {
//            List resultSet = _dbEngine.execQuery("SELECT * FROM privilege");
//            for (Object row : resultSet) {
//                System.out.println(row) ;
//            }
            result.setText("the data");
 //       }
        result.setAttribute("trusty", "true");
        result.setAttribute("other", "smith");
        return result;
    }

    private boolean configure() {
        if (! configured) {
            configureSession("org.hibernate.dialect.PostgreSQLDialect",
                            "org.postgresql.Driver", "jdbc:postgresql:testDB",
                            "postgres", "yawl", null);
            configured = true;
        }
        return configured;
    }

    public void updateFromTaskCompletion(String paramName, Element outputData) {
        
        // retrieve data values from child elements
    }


    public String getDescription() {
        return "A simple example of an external database gateway implementation";
    }

    public Element populateWorkItemData(YSpecificationID specID, String taskName,
                                        Element wiDataTemplate, Element caseData) {
        return null;
    }

    public void updateFromTaskCompletion(String paramName, Element outputData, Element caseData) {}

    
    public Element populateCaseData(YSpecificationID specID, Element caseDataTemplate) {
        return null; 
    }
}
