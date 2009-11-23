package org.yawlfoundation.yawl.editor.thirdparty.engine;

import org.yawlfoundation.yawl.editor.data.DataVariable;
import org.yawlfoundation.yawl.editor.data.WebServiceDecomposition;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.engine.interfce.interfaceA.InterfaceA_EnvironmentBasedClient;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceB_EngineBasedClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

/**
 * Author: Michael Adams - extracted from AvailableEngineProxyImplementation
 * Creation Date: 20/02/2009
 */
public class ConnectableEngineProxyImplementation extends AvailableEngineProxyImplementation {

    public void connect() {
      try {
        if (!connected()) {
          tryConnect();
        }
      } catch (Exception e) {
        sessionID = "";
      }
    }

    public boolean testConnection(String engineURL, String engineUserId, String engineUserPassword) {
     String testSessionID;
      try {
        testSessionID = tryConnect(engineURL, engineUserId, engineUserPassword);
      } catch (Exception e) {
        testSessionID = null;
      }
      return checkConnectionForSessionID(testSessionID);
    }


    public boolean isConnectable() { return true; }

    private String tryConnect(String uri, String userID, String password) {
      if ((userID == null) || (userID.length() == 0))
         return "<failure>No userid specified.</failure>";
      else if ((password == null) || (password.length() == 0))
         return "<failure>No password specified.</failure>";
      else {
        if ((clientInterfaceA == null) || (engineURI == null) || (! uri.equals(engineURI))) {
          engineURI = uri;
          clientInterfaceA = new InterfaceA_EnvironmentBasedClient(engineURI);
        }
        try {
          sessionID = clientInterfaceA.connect(userID, password);
          return sessionID;
        }
        catch (Exception e) {
          return "<failure>Exception attempting to connect to Engine.</failure>";
        }
      }
    }

    private void tryConnect() {
      sessionID = tryConnect(
          prefs.get("engineURI",
              DEFAULT_ENGINE_URI),
          prefs.get("engineUserID",
              DEFAULT_ENGINE_ADMIN_USER),
          prefs.get("engineUserPassword",
               DEFAULT_ENGINE_ADMIN_PASSWORD)
      );
      if (sessionID.startsWith("<failure")) sessionID = "";
    }

    public HashMap getRegisteredYAWLServices() {
      HashMap servicesForEditor = new HashMap();

      servicesForEditor.put(
          WebServiceDecomposition.DEFAULT_ENGINE_SERVICE_NAME,
          null
      );

      if (!connected()) {
        connect();
      }

      if (connected()) {
        Set services = clientInterfaceA.getRegisteredYAWLServices(sessionID);

        Iterator servicesIterator = services.iterator();
        while(servicesIterator.hasNext()) {
          YAWLServiceReference serviceReference = (YAWLServiceReference) servicesIterator.next();


          if (!serviceReference.canBeAssignedToTask()) {
            continue;  // ignore services that are not for tasks.
          }

          // Short and sweet description for the editor.
          String doco = serviceReference.getDocumentation();
          if (doco == null) doco = serviceReference.get_serviceName();
          servicesForEditor.put(doco, serviceReference.getURI());
        }
      }
      return servicesForEditor;
    }

    public boolean connected() {
      if (sessionID == null || sessionID.equals("")) {
        return false;
      }

      if (checkConnectionForSessionID(sessionID)) {
        return true;
      }

     sessionID = "";
     return false;
    }

    private boolean checkConnectionForSessionID(String sessionID) {
      String simplePing = null;
      try {
        simplePing = clientInterfaceA.checkConnection(sessionID);
      } catch (Exception e) {}

        return simplePing != null &&
               simplePing.trim().equals("<response>Permission Granted</response>");
    }




    public LinkedList getEngineParametersForRegisteredService(String registeredYAWLServiceURI) {
      LinkedList dataVariableList = new LinkedList();

      YAWLServiceReference registeredService = null;

      if(!connected()) {
        connect();
      }

      if (connected()) {
        Set services = clientInterfaceA.getRegisteredYAWLServices(sessionID);

        Iterator servicesIterator = services.iterator();
        while(servicesIterator.hasNext()) {
          YAWLServiceReference serviceReference = (YAWLServiceReference) servicesIterator.next();

          if (serviceReference.getURI().equals(registeredYAWLServiceURI)) {
            registeredService = serviceReference;
            break;
          }
        }
        if (registeredService == null) {
          return null;
        }

        YParameter[] engineParametersForService;

        try {
            engineParametersForService = new InterfaceB_EngineBasedClient().getRequiredParamsForService(
                registeredService
            );
        } catch (Exception ioe) {
          return null;
        }


        for(int i = 0; i < engineParametersForService.length; i++) {
          DataVariable editorVariable = new DataVariable();

          editorVariable.setDataType(engineParametersForService[i].getDataTypeName());
          editorVariable.setName(engineParametersForService[i].getName());
          editorVariable.setInitialValue(engineParametersForService[i].getInitialValue());
          editorVariable.setUserDefined(false);

           if (engineParametersForService[i].isInput()) {
             editorVariable.setUsage(DataVariable.USAGE_INPUT_ONLY);
           }
           if (engineParametersForService[i].isOutput()) {
             editorVariable.setUsage(DataVariable.USAGE_OUTPUT_ONLY);
           }

           dataVariableList.add(editorVariable);
        }

        // There's gotta be a nicer way to do this -- I'm having a blond day.

        Object[] dataVariableArray = dataVariableList.toArray();

        for(int i = 0; i < dataVariableArray.length; i++) {
          for(int j = 0; j < dataVariableArray.length; j++) {
            DataVariable iVariable = (DataVariable) dataVariableArray[i];
            DataVariable jVariable = (DataVariable) dataVariableArray[j];

            if (i != j && iVariable.getName() != null && jVariable.getName()!= null &&
                iVariable.getName().equals(jVariable.getName())) {


              // assumption: same name more than once means that it's two paramaters
              // of same name and type, one for input and one for output.  That's
              // a safe assumption for the most part, but the engine DOES allow same
              // name different types as a possibility.

              iVariable.setUsage(DataVariable.USAGE_INPUT_AND_OUTPUT);
              jVariable.setName(null);
            }
          }
        }

        // turfing the unnecessary variables.

        dataVariableList = new LinkedList();

        for(int i = 0; i < dataVariableArray.length; i++) {
          DataVariable iVariable = (DataVariable) dataVariableArray[i];
          if (iVariable.getName() != null) {
            dataVariableList.add(dataVariableArray[i]);
          }
        }
      }
      return dataVariableList;
    }
}




