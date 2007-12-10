/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.admintool.model;

import org.yawlfoundation.yawl.elements.YVerifiable;
import org.yawlfoundation.yawl.util.YVerificationMessage;

import java.util.List;
import java.util.ArrayList;

/**
 * 
 * @author Lachlan Aldred
 * Date: 22/09/2005
 * Time: 16:23:34
 */
public class HumanResource extends Resource implements YVerifiable {
    private String surname;
    private String givenName;
    private String password;
    private boolean isAdministrator = false;
    private String password2;

    private HumanResource() {
        super();
    }

    public HumanResource(String rsrcID) {
        super(rsrcID);
        setIsOfResSerPosType("Resource");
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswords(String password, String password2) {
        this.password = password;
        this.password2 = password2;
    }

    public boolean getIsAdministrator() {
        return isAdministrator;
    }

    public void setIsAdministrator(boolean isAdministrator) {
        this.isAdministrator = isAdministrator;
    }

    public List verify() {
        List result = new ArrayList();
        result.addAll(super.verify());

        if(password == null || password.length() < 4){
            result.add(new YVerificationMessage(
                    this,
                    "Password must be at least 4 chars long.",
                    YVerificationMessage.ERROR_STATUS));
        }
        if(!password2.equals(password)){
            result.add(new YVerificationMessage(
                    this,
                    "Passwords must be the same.",
                    YVerificationMessage.ERROR_STATUS));
        }
        return result;
    }

}