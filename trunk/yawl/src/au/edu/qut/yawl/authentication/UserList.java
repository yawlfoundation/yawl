/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retreived from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are commited to improving workflow technology.
 *
 */


package au.edu.qut.yawl.authentication;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import au.edu.qut.yawl.admintool.model.HumanResource;
import au.edu.qut.yawl.engine.AbstractEngine;
import au.edu.qut.yawl.engine.EngineFactory;
import au.edu.qut.yawl.engine.YEngine;
import au.edu.qut.yawl.engine.domain.YWorkItem;
import au.edu.qut.yawl.exceptions.YAuthenticationException;
import au.edu.qut.yawl.persistence.dao.DAO;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction;
import au.edu.qut.yawl.persistence.dao.restrictions.Unrestricted;
import au.edu.qut.yawl.persistence.dao.restrictions.PropertyRestriction.Comparison;


/**
 * 
 * @author Lachlan Aldred
 * Date: 27/01/2004
 * Time: 17:23:05
 * 
 */
public class UserList {
    private Map _connections;
    private Random _random;
    private static UserList _myInstance;

    public static String _permissionGranted = "Permission Granted";


    public static UserList getInstance(){
        if(_myInstance == null){
            _myInstance = new UserList();
        }
        return _myInstance;
    }

    private void setIsAdmin(String userName, boolean isAdmin) {
        HumanResource user = (HumanResource) getUser(userName);
        if(user != null){

            user.setIsAdministrator(isAdmin);
        }
    }


    private UserList() {

        this._connections = new HashMap();
        this._random = new Random();
    }

    public String checkConnection(String sessionHandle) throws YAuthenticationException{
        Connection connection = (Connection) _connections.get(sessionHandle);
        Date now = new Date();
        if(connection == null){
            throw new YAuthenticationException(
                    "There is no registered connection for " + sessionHandle);
        }
        else if(now.after(connection._timeOut)){
            throw new YAuthenticationException(
                    "Connection for session (" + sessionHandle +
                    ") has timed out");
        }
        return _permissionGranted;
    }


    /**
     * Checks the connection for administraive priviledges.
     * @param sessionHandle the user sessionhandle
     * @throws YAuthenticationException if the connection is not allowed.
     */
    public String checkConnectionForAdmin(String sessionHandle) throws YAuthenticationException {
        checkConnection(sessionHandle);
        Connection connection = (Connection) _connections.get(sessionHandle);
        HumanResource user = (HumanResource) getUser(connection._userid);
        if (!user.getIsAdministrator()) {
            throw new YAuthenticationException(
                    "This user is not an administrator.");
        }
        return _permissionGranted;
    }




    /**
     * Adds a new user into the system (ENGINE INTERFACE)<P>
     *
     * @param userID
     * @param password
     * @param isAdmin
     * @throws YAuthenticationException
     */
    // FIXME: XXX the password validity check should be made into some kind of security plug-in.
    public HumanResource addUser(String userID, String password, boolean isAdmin) throws YAuthenticationException {
    	HumanResource human = null;
        if (userID != null){
            if (password != null && password.length() > 3) {
                if (getUser(userID)==null) {

                	DAO dao = EngineFactory.getExistingEngine().getDao();
                	human = new HumanResource(userID);
                	human.setPassword(password);
                	human.setGivenName(userID);
                	human.setSurname(" ");
                	human.setIsAdministrator(isAdmin);
                	try {
                		dao.save(human);
                	} catch (Exception e) {
                		e.printStackTrace();
                	}
                	
                } else {
                    throw new YAuthenticationException(
                        "The userID[" + userID + "] is being used already.");
                }
            } else {
                throw new YAuthenticationException("Password must be at least 4 chars.");
            }
        } else {
            throw new YAuthenticationException("UserID cannot be null.");
        }
        return human;
    }

    public synchronized void removeUser(String inSessionUserID, String userNameToDelete) throws YAuthenticationException {
        if(inSessionUserID.equals(userNameToDelete)) {
            throw new YAuthenticationException("Users cannot delete oneself.");
        }
        if(null == getUser(inSessionUserID)){
            throw new YAuthenticationException("The user trying to delete is not a user.");
        }
        List connections = new ArrayList(_connections.values());
        for (int i = 0; i < connections.size(); i++) {
            Connection connection = (Connection) connections.get(i);
            if(connection._userid.equals(userNameToDelete)){
                _connections.remove(connection._sessionHandle);
            }
        }

        removeUser(userNameToDelete);
    }

    public void removeUser(String userID) {
    	try {DAO dao = EngineFactory.getExistingEngine().getDao();
    		HumanResource human = getUser(userID);
    		if (human!=null) {
    			dao.delete(human);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }

    public String connect(String userID, String password) throws YAuthenticationException{
        HumanResource user = (HumanResource) getUser(userID);
        if(user != null){
            if(user.getPassword().equals(password)){
                Connection connection = new Connection(userID);
                _connections.put(connection._sessionHandle, connection);
                return connection._sessionHandle;
            }
            throw new YAuthenticationException("Password (" + password + ") not valid.");
        }
        throw new YAuthenticationException("Userid (" + userID + ") not valid.");
    }


    public String getUserID(String sessionHandle) {
        Connection connection = (Connection) _connections.get(sessionHandle);
        return connection != null ? connection._userid : null;
    }


    public Set<HumanResource> getUsers() {
        Set<HumanResource> result = new HashSet();
        
    	DAO dao = EngineFactory.getExistingEngine().getDao();
    	try {
    		List<HumanResource> humans = dao.retrieveByRestriction(HumanResource.class, 
    			new Unrestricted());

    		result = new HashSet<HumanResource>(humans);
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return result;
        
   }

    
    public HumanResource getUser(String userID) {
    	DAO dao = EngineFactory.getExistingEngine().getDao();
    	try {
    		List<HumanResource> humans = dao.retrieveByRestriction(HumanResource.class, 
    			new PropertyRestriction("rsrcID", Comparison.EQUAL, userID));
    			
    		if (humans.size()>=1) {
    			return humans.get(0);
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    		

    
    
    
    // FIXME: XXX the password is not checked to make sure it's valid... (very bad)
    public void changePassword(String userID, String password) {
        HumanResource user = (HumanResource) getUser(userID);
        user.setPassword(password);
        DAO dao = EngineFactory.getExistingEngine().getDao();
        try  {
        	dao.save(user);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
    }


    public void clear() {
        DAO dao = EngineFactory.getExistingEngine().getDao();
        try  {
        	Set users = getUsers();
        	Iterator it = users.iterator();
        	while (it.hasNext()) {
        		dao.delete(it.next());
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        _connections = new HashMap();
    }




    //#####################################################################################
    //                                  Private Classes
    //#####################################################################################

    private class Connection{
        String _sessionHandle;
        Date _timeOut;
        String _userid;
        public Connection(String userid) {
            _sessionHandle = "" + Math.abs(_random.nextLong());
            Date tenHoursFromNow = new Date((10 * 60 * 60 * 1000) + new Date().getTime());
            _timeOut = tenHoursFromNow;
            _userid = userid;
        }
    }

}
