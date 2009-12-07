/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.engine.interfce.interfaceA;

import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 /**
 * A client side API for the management of processes, services and users
 *
 * @author Lachlan Aldred
 * Date: 16/04/2004
 * Time: 16:15:02
 *
 * @author Michael Adams (refactored for v2.0, 06/2008, and v2.1 11/2009)
 * 
 */
public class InterfaceA_EnvironmentBasedClient extends Interface_Client {
    private String _backEndURIStr;


    /**
     * Constructor
     * @param backEndURIStr the back end uri of where to find
     * the engine.  In a default deployment, this value is
     * "http://localhost:8080/yawl/ia" but it can be changed.
     */
    public InterfaceA_EnvironmentBasedClient(String backEndURIStr) {
        _backEndURIStr = backEndURIStr;
    }


    /**
     * Creates a user session with the engine. Returns a sessionhandle for use in all
     * other engine access methods.
     * @param userID a valid user ID
     * @param password a valid password
     * @return the sessionHandle - expires after one hour.
     * @throws IOException if there's a problem connecting to the engine
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userID", userID);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executePost(_backEndURIStr, params);
    }


    /**
     * Checks that a session handle is active
     * @param sessionHandle the handle to check
     * @return true if the handle is active, false if not
     * @throws IOException if there's a problem connecting to the engine
     */
    public String checkConnection(String sessionHandle) throws IOException {
        return executeGet(_backEndURIStr,
                          prepareParamMap("checkConnection", sessionHandle));
    }


    /******* SERVICES *************************************************************/

    /**
     * Registers a new custom YAWL service woth the engine.
     * @param service the service.
     * @param sessionHandle a valid sessionhandle
     * @return a diagnostic XML message.
     * @throws IOException if something goes awry.
     */
    public String addYAWLService(YAWLServiceReference service, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("newYAWLService", sessionHandle);
        params.put("service", service.toXMLComplete());
        return executePost(_backEndURIStr, params);
    }


    /**
     * Removes a YAWL service from the engine.
     * @param serviceURI the service URI.
     * @param sessionHandle a valid sessionhandle.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String removeYAWLService(String serviceURI, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("removeYAWLService", sessionHandle);
        params.put("serviceURI", serviceURI);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Returns a list of YAWL service objects registered with the engine.
     * @param sessionHandle a valid session handle
     * @return the set of active yawl services
     */
    public Set<YAWLServiceReference> getRegisteredYAWLServices(String sessionHandle) {
        Set<YAWLServiceReference> result = new HashSet<YAWLServiceReference>();
        try {
            String xml = getRegisteredYAWLServicesAsXML(sessionHandle);
            if (xml != null && successful(xml)) {
                Document doc = JDOMUtil.stringToDocument(xml);

                for (Object o : doc.getRootElement().getChildren()) {
                    Element service = (Element) o;
                    result.add(YAWLServiceReference.unmarshal(JDOMUtil.elementToString(service)));
                }
            }
        } catch (IOException e) {
            InterfaceBWebsideController.logContactError(e, _backEndURIStr);
        }
        return result;
    }


    /**
     * Returns an XML string list of YAWL services registered with the engine.
     * @param sessionHandle an active handle
     * @return XML string list of services or a diagnostic error message
     * @throws IOException if there's a problem connecting to the engine
     */
    public String getRegisteredYAWLServicesAsXML(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getYAWLServices", sessionHandle);
        return executeGet(_backEndURIStr, params);
    }


    /******* SPECIFICATIONS ******************************************************/

    /**
     * Uploads a specification into the engine.
     * @param specification this is *not* a file name, this is the entire specification
     * xml file in string format.
     * @param sessionHandle a sessionhandle.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String uploadSpecification(String specification,
                                      String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("upload", sessionHandle);
        params.put("specXML", specification);
        return executePost(_backEndURIStr, params);
    }

    /**
     * Uploads a specification into the engine.
     * @deprecated use uploadSpecification(String, String) instead (since 2.0)
     * @param specification this is *not* a file name, this is the entire specification
     * xml file in string format.
     * @param filename the file name of the specification xml file (no longer used since 2.0)
     * @param sessionHandle a sessionhandle.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String uploadSpecification(String specification, String filename,
                                      String sessionHandle) throws IOException {
        return uploadSpecification(specification, sessionHandle);
    }


    /**
     * Uploads a specification into the engine.
     * @param file the file name of the specification xml file
     * @param sessionHandle a sessionhandle.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String uploadSpecification(File file,
                                      String sessionHandle) throws IOException {
        String specification = StringUtil.fileToString(file.getAbsolutePath());
        return uploadSpecification(specification, sessionHandle);
    }



    /**
     * Unloads a loaded specification from the engine
     * @deprecated superceded by unloadSpecification(YSpecificationID, String) - this
     *             version is appropriate for pre-2.0 schema-based specs only
     * @param specID the id of the specification to unload
     * @param sessionHandle a sessionhandle.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String unloadSpecification(String specID, String sessionHandle) throws IOException {
        return unloadSpecification(new YSpecificationID(specID), sessionHandle);
    }


    /**
     * Unloads a loaded specification from the engine
     * @param specID the id of the specification to unload
     * @param sessionHandle a sessionhandle.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String unloadSpecification(YSpecificationID specID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("unload", sessionHandle);
        params.putAll(specID.toMap());
        return executePost(_backEndURIStr, params);
    }


    /******* EXTERNAL ACCOUNTS ****************************************************/

    /**
     * Creates a new user inside the engine.
     * @param name the new username
     * @param password the new password
     * @param documentation some descriptive text about the account
     * @param sessionHandle a current valid sessionhandle
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String addClientAccount(String name, String password, String documentation,
                             String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("createAccount", sessionHandle);
        params.put("userID", name);
        params.put("password", password);
        params.put("doco", documentation);
        return executePost(_backEndURIStr, params);
    }


    public String addClientAccount(YExternalClient client, String sessionHandle)
            throws IOException {
        return addClientAccount(client.getUserID(), client.getPassword(),
                client.getDocumentation(), sessionHandle);
    }

    
    /**
     * Gets all the client accounts registered in the engine
     * @param sessionHandle a current valid sessionhandle of an admin type user.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public Set<YExternalClient> getClientAccounts(String sessionHandle) throws IOException {
        Set<YExternalClient> accounts = new HashSet<YExternalClient>();
        Map<String, String> params = prepareParamMap("getAccounts", sessionHandle);
        String result = executeGet(_backEndURIStr, params);

        if (successful(result)) {
            Document doc = JDOMUtil.stringToDocument(result);
            if (doc != null) {
                List children = doc.getRootElement().getChildren();
                for (Object o : children) {
                    Element clientElem = (Element) o;

                    // add clients, but ignore generic admin user
                    if (! clientElem.getChildText("username").equals("admin")) {
                        accounts.add(new YExternalClient((Element) o));
                    }
                }
            }
        }
        return accounts ;
    }


    /**
     * Delete an external client account
     * PREcondition: cannot delete self
     * @param name the user to delete
     * @param sessionHandle an active handle
     * @return diagnostic string of results from engine.
     * @throws IOException if there's a problem connecting to the engine
     */
    public String removeClientAccount(String name, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("deleteAccount", sessionHandle);
        params.put("userID", name);
        return executePost(_backEndURIStr, params);
    }

    
    /**
     * Change the password of a service or client account on the engine, the account
     * being the owner of the session handle.
     * @param password the new password
     * @param sessionHandle an active handle
     * @return diagnostic string of results from engine.
     * @throws IOException if there's a problem connecting to the engine
     */
    public String changePassword(String password, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("newPassword", sessionHandle);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        return executePost(_backEndURIStr, params);
    }


}
