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
import org.yawlfoundation.yawl.authentication.User;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.engine.interfce.interfaceB.InterfaceBWebsideController;
import org.yawlfoundation.yawl.util.JDOMUtil;

import java.io.IOException;
import java.util.*;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 16/04/2004
 * Time: 16:15:02
 *
 * @author Michael Adams (refactored for v2.0, 06/2008)
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


    public String checkConnection(String sessionHandle) throws IOException {
        return executeGet(_backEndURIStr,
                          prepareParamMap("checkConnection", sessionHandle));
    }


    /**
     * Change the password of a user on the engine.
     * The person's password is the one that owns the session handle.
     * @param password
     * @param sessionHandle
     * @return diagnostic string of results from engine.
     * @throws IOException
     */
    public String changeUserPassword(String password, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("newPassword", sessionHandle);
        params.put("password", password);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Delete the user
     * PREcondition: cannot delete self AND
     *               must be an Admin
     * @param username
     * @param sessionHandle
     * @return
     */
    public String deleteUser(String username, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("deleteUser", sessionHandle);
        params.put("userID", username);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Returns a sessionhandle for use in all other engine access methods.  It is used for
     * to achieve "admin level" access.
     * @param userID a valid user ID
     * @param password a valid password
     * @return the sessionHandle - expires after one hour.
     */
    public String connect(String userID, String password) throws IOException {
        Map<String, String> params = prepareParamMap("connect", null);
        params.put("userID", userID);
        params.put("password", password);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Returns a list of YAWL service objects registered with the engine.
     * @param sessionHandle the session handle - won't work without the correct one.
     * @return te set of active yawl services
     */
    public Set<YAWLServiceReference> getRegisteredYAWLServices(String sessionHandle) {
        Set<YAWLServiceReference> result = new HashSet<YAWLServiceReference>();
        try {
            String xml = getRegisteredYAWLServicesAsXML(sessionHandle);
            if (xml != null && successful(xml)) {
                Document doc = JDOMUtil.stringToDocument(xml);
                Iterator yawlServiceIter = doc.getRootElement().getChildren().iterator();

                while (yawlServiceIter.hasNext()) {
                    Element service = (Element) yawlServiceIter.next();
                    result.add(
                       YAWLServiceReference.unmarshal(JDOMUtil.elementToString(service)));
                }
            }
        } catch (IOException e) {
            InterfaceBWebsideController.logContactError(e, _backEndURIStr);
        }
        return result;
    }

    
    public String getRegisteredYAWLServicesAsXML(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getYAWLServices", sessionHandle);
        return executeGet(_backEndURIStr, params);
    }


    /**
     * Lets the engine know of a new custom YAWL service avaliable.
     * @param service the service.
     * @param sessionHandle a valuid sessionhandle
     * @return a diagnostic XML message.
     * @throws IOException if something goes awry.
     */
    public String setYAWLService(YAWLServiceReference service, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("newYAWLService", sessionHandle);
        params.put("service", service.toXMLComplete());
        return executePost(_backEndURIStr, params);
    }


    /**
     * Removes a YAWL service from the engine.
     * @param serviceURI the service URI.
     * @param sessionHandle a sessionhandle.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String removeYAWLService(String serviceURI, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("removeYAWLService", sessionHandle);
        params.put("serviceURI", serviceURI);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Uploads a specification into the engine.
     * @param specification this is not file name this is the entire specification
     * in string format.
     * @param sessionHandle
     * @return a result message indicting failure/success and some diagnostics
     */
    public String uploadSpecification(String specification, String filename,
                                      String sessionHandle) throws IOException {
        return executeUpload(_backEndURIStr + "/upload", specification, filename,
                             sessionHandle);
    }

    
    public String unloadSpecification(String specID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("unload", sessionHandle);
        params.put("specID", specID);
        return executePost(_backEndURIStr, params);
    }


    public String unloadSpecification(YSpecificationID specID, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("unload", sessionHandle);
        params.put("specID", specID.getSpecName());
        params.put("version", specID.getVersion().toString());
        return executePost(_backEndURIStr, params);
    }


    /**
     * Creates a new user inside the engine.
     * @param userName the new username
     * @param password the new password
     * @param isAdmin true if the new user should have admin priviledges.
     * @param sessionHandle a current valid sessionhandle of an admin type user.
     * @return a diagnostic XML result message.
     * @throws IOException
     */
    public String createUser(String userName, String password, boolean isAdmin, String sessionHandle) throws IOException {
        String action = isAdmin ? "createAdmin" :"createUser";
        Map<String, String> params = prepareParamMap(action, sessionHandle);
        params.put("userID", userName);
        params.put("password", password);
        return executePost(_backEndURIStr, params);
    }


    public List<User> getUsers(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getUsers", sessionHandle);
        ArrayList<User> users = new ArrayList<User>();
        String result = executeGet(_backEndURIStr, params);

        if (successful(result)) {
            Document doc = JDOMUtil.stringToDocument(result);
            if (doc != null) {
                List userElems = doc.getRootElement().getChildren();
                for (int i = 0; i < userElems.size(); i++) {
                    Element element = (Element) userElems.get(i);
                    String id = element.getChildText("id");
                    User u = new User(id, null);
                    if (element.getChildText("isAdmin").equals("true")) {
                        u.setAdmin(true);
                    }
                    users.add(u);
                }
            }
        }
        return users;
    }


}
