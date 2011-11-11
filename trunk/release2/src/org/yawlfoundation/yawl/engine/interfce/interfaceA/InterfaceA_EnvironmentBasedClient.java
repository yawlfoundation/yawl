/*
 * Copyright (c) 2004-2011 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.engine.interfce.interfaceA;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.yawlfoundation.yawl.authentication.YExternalClient;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.Interface_Client;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.PasswordEncryptor;
import org.yawlfoundation.yawl.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


    public String getBackEndURI() { return _backEndURIStr; }


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
        service.setPassword(PasswordEncryptor.encrypt(service.getPassword(), service.getPassword()));
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
    
    
    public YAWLServiceReference getYAWLService(String serviceURI, String sessionHandle) 
            throws IOException {
        Map<String, String> params = prepareParamMap("getYAWLService", sessionHandle);
        params.put("serviceURI", serviceURI);
        String xml = executePost(_backEndURIStr, params);
        if (xml != null && successful(xml)) {
            YAWLServiceReference service = new YAWLServiceReference();
            service.fromXML(xml);
            return service;
        }
        return null;
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
        } catch (IOException ioe) {
              Logger.getLogger(this.getClass()).error(
                "Problem contacting YAWL Engine at URI [" + _backEndURIStr + "]", ioe);
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
        params.put("password", PasswordEncryptor.encrypt(password, null));
        params.put("doco", documentation);
        return executePost(_backEndURIStr, params);
    }


    public String addClientAccount(YExternalClient client, String sessionHandle)
            throws IOException {
        return addClientAccount(client.getUserName(), client.getPassword(),
                client.getDocumentation(), sessionHandle);
    }


    public String updateClientAccount(String name, String password, String documentation,
                             String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("updateAccount", sessionHandle);
        params.put("userID", name);
        params.put("password", PasswordEncryptor.encrypt(password, null));
        params.put("doco", documentation);
        return executePost(_backEndURIStr, params);
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
                    accounts.add(new YExternalClient((Element) o));
                }
            }
        }
        return accounts ;
    }

    public YExternalClient getClientAccount(String userID, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("getClientAccount", sessionHandle);
        params.put("userID", userID);
        String result = executeGet(_backEndURIStr, params);

        if (successful(result)) {
            Element e = JDOMUtil.stringToElement(result);
            if (e != null) {
                return new YExternalClient(e);
            }
        }
        return null ;
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


    public String getPassword(String userid, String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getPassword", sessionHandle);
        params.put("userID", userid);
        return executePost(_backEndURIStr, params);
    }

    public String getBuildProperties(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getBuildProperties", sessionHandle);
        return executeGet(_backEndURIStr, params);
    }

    public String getExternalDBGateways(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getExternalDBGateways", sessionHandle);
        return executeGet(_backEndURIStr, params);
    }


    public String setHibernateStatisticsEnabled(boolean enabled, String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("setHibernateStatisticsEnabled", sessionHandle);
        params.put("enabled", String.valueOf(enabled));
        return executePost(_backEndURIStr, params);
    }

    public String isHibernateStatisticsEnabled(String sessionHandle)
            throws IOException {
        Map<String, String> params = prepareParamMap("isHibernateStatisticsEnabled", sessionHandle);
        return executeGet(_backEndURIStr, params);
    }

    public String getHibernateStatistics(String sessionHandle) throws IOException {
        Map<String, String> params = prepareParamMap("getHibernateStatistics", sessionHandle);
        return executeGet(_backEndURIStr, params);
    }


}
