/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.engine.interfce;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import au.edu.qut.yawl.authentication.User;
import au.edu.qut.yawl.elements.YAWLServiceReference;

/**
 /**
 * 
 * @author Lachlan Aldred
 * Date: 16/04/2004
 * Time: 16:15:02
 * 
 */
public class InterfaceA_EnvironmentBasedClient extends Interface_Client {
    private String _backEndURIStr;
    private SAXBuilder _builder = new SAXBuilder();;


    /**
     * Constructor
     * @param backEndURIStr the back end uri of where to find
     * the engine.  In a default deployment, this value is
     * "http://localhost:8080/yawl/ia" but it can be changed.
     */
    public InterfaceA_EnvironmentBasedClient(String backEndURIStr) {
        _backEndURIStr = backEndURIStr;
    }


    public String checkConnection(String sessionHandle) {
        if (sessionHandle == null) return null ;
        try {
        	Map params = new HashMap();
        	params.put("action", "checkConnection");
        	params.put("sessionHandle", sessionHandle);
            return executeGet(_backEndURIStr, params);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
        Map map = new HashMap();
        map.put("action", "newPassword");
        map.put("password", password);
        map.put("sessionHandle", sessionHandle);

        return executePost(_backEndURIStr, map);
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
        Map map = new HashMap();
        map.put("action", "deleteUser");
        map.put("userName", username);
        map.put("sessionHandle", sessionHandle);

        return executePost(_backEndURIStr, map);
    }


    /**
     * Returns a sessionhandle for use in all other engine access methods.  It is used for
     * to achieve "admin level" access.
     * @param userID a valid user ID
     * @param password a valid password
     * @return the sessionHandle - expires after one hour.
     */
    public String connect(String userID, String password) throws IOException {
        Map queryMap = new HashMap();
        queryMap.put("userid", userID);
        queryMap.put("password", password);
        queryMap.put( "action", "connect" );
        return executePost(_backEndURIStr, queryMap);
    }


    /**
     * Returns a list of YAWL service objects registered with
     * the engine.
     * @param sessionHandle the session handle - won't work without the correct one.
     * @return
     */
    public Set getRegisteredYAWLServices(String sessionHandle) {
        Set yawlServices = new HashSet();
        try {
        	Map params = new HashMap();
        	params.put("action", "getYAWLServices");
        	params.put("sessionHandle", sessionHandle);
            String result = executeGet(_backEndURIStr, params);
            SAXBuilder builder = new SAXBuilder();
            if (result != null && successful(result)) {
                Document doc = null;
                doc = builder.build(new StringReader(result));
                Iterator yawlServiceIter = doc.getRootElement().getChildren().iterator();
                XMLOutputter out = new XMLOutputter(Format.getCompactFormat());

                while (yawlServiceIter.hasNext()) {
                    Element yawlServiceElem = (Element) yawlServiceIter.next();
                    YAWLServiceReference service =
                            YAWLServiceReference.unmarshal(out.outputString(yawlServiceElem));
                    yawlServices.add(service);
                }
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            InterfaceBWebsideController.logContactError(e, _backEndURIStr);
        }
        return yawlServices;
    }


    /**
     * Lets the engine know of a new custom YAWL service avaliable.
     * @param service the service.
     * @param sessionHandle a valuid sessionhandle
     * @return a diagnostic XML message.
     * @throws IOException if something goes awry.
     */
    public String setYAWLService(YAWLServiceReference service, String sessionHandle) throws IOException {
        String serialisedYAWLService = service.toXML();
        Map queryMap = new HashMap();
        queryMap.put("sessionHandle", sessionHandle);
        queryMap.put("action", "newYAWLService");
        queryMap.put("service", serialisedYAWLService);
        return executePost(_backEndURIStr, queryMap);
    }


    /**
     * Removes a YAWL service from the engine.
     * @param serviceURI the service URI.
     * @param sessionHandle a sessionhandle.
     * @return a diagnostic XML result message.
     * @throws IOException if bad connection.
     */
    public String removeYAWLService(String serviceURI, String sessionHandle) throws IOException {
        Map queryMap = new HashMap();
        queryMap.put("sessionHandle", sessionHandle);
        queryMap.put("action", "removeYAWLService");
        queryMap.put("serviceURI", serviceURI);
        return executePost(_backEndURIStr, queryMap);
    }


    /**
     * Uploads a specification into the engine.
     * @param specification this is not file name this is the entire specification
     * in string format.
     * @param sessionHandle
     * @return a result message indicting failure/success and some diagnostics
     */
    public String uploadSpecification(String specification, String filename, String sessionHandle) throws IOException {
        return executeUpload(specification, filename, sessionHandle);
    }
    
    private String executeUpload(String specification, String filename, String sessionHandle) throws IOException {
    	Map params = new HashMap();
    	params.put( "action", "upload" );
    	params.put( "sessionHandle", sessionHandle );
    	params.put( "filename", filename );
    	params.put( "specification", specification );
    	return executePost(_backEndURIStr, params);
    }

    public String unloadSpecification(String specID, String sessionHandle) throws IOException {
        Map params = new HashMap();
        params.put("action", "unload");
        params.put("sessionHandle", sessionHandle);
        params.put("specID", specID);
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
        Map params = new HashMap();
        params.put("userName", userName);
        params.put("password", password);
        if (isAdmin) {
            params.put("action", "createAdmin");
        } else {
            params.put("action", "createUser");
        }
        params.put("sessionHandle", sessionHandle);
        return executePost(_backEndURIStr, params);
    }

    public List getUsers(String sessionHandle) {
        String result = null;
        try {
        	Map params = new HashMap();
        	params.put("action", "getUsers");
        	params.put("sesionHandle", sessionHandle);
            result = executeGet(_backEndURIStr, params);
        } catch (IOException e) {
            InterfaceBWebsideController.logContactError(e, _backEndURIStr);
        }
        ArrayList users = new ArrayList();
        if (successful(result)) {
            try {
                Document doc = _builder.build(new StringReader(result));
                List userElems = doc.getRootElement().getChildren();
                for (int i = 0; i < userElems.size(); i++) {
                    Element element = (Element) userElems.get(i);
                    String id = element.getChildText("id");
                    String isAdmin = element.getChildText("isAdmin");
                    User u = new User(id, null);
                    if (isAdmin.equals("true")) {
                        u.setAdmin(true);
                    }
                    users.add(u);
                }
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return users;
    }


}
