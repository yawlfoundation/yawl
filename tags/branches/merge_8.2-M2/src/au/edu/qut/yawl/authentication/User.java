/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package au.edu.qut.yawl.authentication;

/**
 /** 
 * 
 * @author Lachlan Aldred
 * Date: 19/04/2004
 * Time: 15:59:23
 * 
 */
public class User {
    /*
      MODIFIED FOR PERSISTANCE
     */
    String userID;
    String password;
    private boolean isAdmin = false;

    public User(String userID, String password) {
        this.userID = userID;
        this.password = password;
    }

    public String getUserID() {
        return userID;
    }

    /*
      inserted for persistance
     */
    public User() {
    }
    public void setUserID(String userID) {
	this.userID = userID;
    }
    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
    public boolean getIsAdmin() {
        return isAdmin;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPassword() {
        return password;
    }
    /*******************************/

    public String toString() {
        return "<userid>" + userID + "</userid>" +
                "<password>" + password + "</password>";
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public String toXML() {
        StringBuffer result = new StringBuffer();
        result.append("<user>");
        result.append("<id>" + userID + "</id>");
        result.append("<isAdmin>" + isAdmin + "</isAdmin>");
        result.append("</user>");
        return result.toString();
    }
}
