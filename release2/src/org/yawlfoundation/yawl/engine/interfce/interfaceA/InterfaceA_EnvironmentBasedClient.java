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
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
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


    /**
     * Checks that a seesion handle is active
     * @param sessionHandle the handle to check
     * @return true if the handle is active
     * @throws IOException if there's a problem connecting to the engine
     */
    public String checkConnection(String sessionHandle) throws IOException {
        return executeGet(_backEndURIStr,
                          prepareParamMap("checkConnection", sessionHandle));
    }


    /**
     * Change the password of a user on the engine.
     * The person's password is the one that owns the session handle.
     * @param password the new password
     * @param sessionHandle an active handle
     * @return diagnostic string of results from engine.
     * @throws IOException if there's a problem connecting to the engine
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
     * @param username the user to delete
     * @param sessionHandle an active handle
     * @return diagnostic string of results from engine.
     * @throws IOException if there's a problem connecting to the engine
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
     * @throws IOException if there's a problem connecting to the engine
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
     * @deprecated use uploadSpecification(String, String) instead (since 2.0)
     * Uploads a specification into the engine.
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
     * @deprecated superceded by unloadSpecification(YSpecification, String)
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
        params.put("specID", specID.getSpecName());
        params.put("version", specID.getVersionAsString());
        return executePost(_backEndURIStr, params);
    }


    /**
     * Creates a new user inside the engine.
     * @param userName the new username
     * @param password the new password
     * @param isAdmin true if the new user should have admin priviledges.
     * @param sessionHandle a current valid sessionhandle of an admin type user.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String createUser(String userName, String password, boolean isAdmin,
                             String sessionHandle) throws IOException {
        String action = isAdmin ? "createAdmin" :"createUser";
        Map<String, String> params = prepareParamMap(action, sessionHandle);
        params.put("userID", userName);
        params.put("password", password);
        return executePost(_backEndURIStr, params);
    }


    /**
     * Gets all the users registered in the engine
     * @param sessionHandle a current valid sessionhandle of an admin type user.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public List<User> getUsers(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getUsers", sessionHandle);
        ArrayList<User> users = new ArrayList<User>();
        String result = executeGet(_backEndURIStr, params);

        if (successful(result)) {
            Document doc = JDOMUtil.stringToDocument(result);
            if (doc != null) {
                List userElems = doc.getRootElement().getChildren();
                int i = 0;
                while (i < userElems.size()) {
                    Element element = (Element) userElems.get(i);
                    String id = element.getChildText("id");
                    User u = new User(id, null);
                    if (element.getChildText("isAdmin").equals("true")) {
                        u.setAdmin(true);
                    }
                    users.add(u);
                    i++;
                }
            }
        }
        return users;
    }


}
