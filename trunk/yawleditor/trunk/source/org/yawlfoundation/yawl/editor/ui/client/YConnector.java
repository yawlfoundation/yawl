package org.yawlfoundation.yawl.editor.ui.client;

import org.yawlfoundation.yawl.editor.core.connection.YEngineConnection;
import org.yawlfoundation.yawl.editor.core.connection.YResourceConnection;
import org.yawlfoundation.yawl.editor.ui.data.DataVariable;
import org.yawlfoundation.yawl.editor.ui.util.LogWriter;
import org.yawlfoundation.yawl.editor.ui.resourcing.*;
import org.yawlfoundation.yawl.elements.YAWLServiceReference;
import org.yawlfoundation.yawl.elements.data.YParameter;
import org.yawlfoundation.yawl.resourcing.AbstractSelector;
import org.yawlfoundation.yawl.resourcing.codelets.CodeletInfo;
import org.yawlfoundation.yawl.resourcing.resource.*;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanCategory;
import org.yawlfoundation.yawl.resourcing.resource.nonhuman.NonHumanResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * A wrapper class of static methods for the connection API
 * (to the engine and resource service)
 * @author Michael Adams
 * @date 14/09/11
 */
public class YConnector {

    private static final YEngineConnection _engConn = new YEngineConnection();
    private static final YResourceConnection _resConn = new YResourceConnection();

    // ensure this is never instantiated - static methods only
    private YConnector() {}


    public static boolean isEngineConnected() {
        return _engConn.isConnected();
    }

    public static boolean isResourceConnected() {
        return _resConn.isConnected();
    }

    public static void disconnectEngine() { _engConn.disconnect(); }

    public static void disconnectResource() { _resConn.disconnect(); }

    public static void setEngineUserID(String id) { _engConn.setUserID(id); }
    public static void setEnginePassword(String pw) { _engConn.setPassword(pw); }
    public static void setEngineURL(URL url) { _engConn.setURL(url); }
    public static void setEngineURL(String url) { _engConn.setURL(makeURL(url)); }

    public static void setResourceUserID(String id) { _resConn.setUserID(id); }
    public static void setResourcePassword(String pw) { _resConn.setPassword(pw); }
    public static void setResourceURL(URL url) { _resConn.setURL(url); }
    public static void setResourceURL(String url) { _resConn.setURL(makeURL(url)); }


    /**
     * Checks whether a valid connection can be made with the parameters passed.
     * @param url the connection's URL
     * @param userid the userid
     * @param password the password
     * @return true if the parameters can be used to create a valid connection
     */
    public static boolean testEngineParameters(String url, String userid, String password) {
        YEngineConnection tempConn = new YEngineConnection(url);
        tempConn.setUserID(userid);
        tempConn.setPassword(password);
        return tempConn.isConnected();
    }


    /**
     * Checks whether a valid connection can be made with the parameters passed.
     * @param url the connection's URL
     * @param userid the userid
     * @param password the password
     * @return true if the parameters can be used to create a valid connection
     */
    public static boolean testResourceServiceParameters(String url, String userid,
                                                        String password) {
        YResourceConnection tempConn = new YResourceConnection(url);
        tempConn.setUserID(userid);
        tempConn.setPassword(password);
        return tempConn.isConnected();
    }


    public static Map<String, String> getExternalDataGateways() throws IOException {
        return _engConn.getExternalDataGateways();
    }

    public static Set<YAWLServiceReference> getServices() throws IOException {
        return _engConn.getRegisteredYAWLServices();
    }
    
    public static YAWLServiceReference getService(String uri) throws IOException {
        return _engConn.getService(uri);
    }


    public static List<String> getParticipantIDs() {
        return _resConn.getParticipantIDs();
    }

    public static List<String> getRoleIDs() {
        return _resConn.getRoleIDs();
    }

    public static List<CodeletInfo> getCodelets() throws IOException {
        return _resConn.getCodelets();
    }

    public static List<Capability> getCapabilities() throws IOException {
        return _resConn.getCapabilities();
    }

    public static List<Position> getPositions() throws IOException {
        return _resConn.getPositions();
    }

    public static List<OrgGroup> getOrgGroups() throws IOException {
        return _resConn.getOrgGroups();
    }

    public static List<NonHumanResource> getNonHumanResources() throws IOException {
        return _resConn.getNonHumanResources();
    }

    public static List<NonHumanCategory> getNonHumanCategories() throws IOException {
        return _resConn.getNonHumanCategories();
    }

    public static List<Participant> getParticipants() throws IOException {
        return _resConn.getParticipants();
    }

    public static List<Role> getRoles() throws IOException {
        return _resConn.getRoles();
    }

    public static boolean hasResources() {
        try {
            return ! (getParticipants().isEmpty() && getRoles().isEmpty());
        }
        catch (IOException ioe) {
            return false;
        }
    }


    public static List<ResourcingParticipant> getResourcingParticipants() {
        List<ResourcingParticipant> pList = new ArrayList<ResourcingParticipant>();
        try {
            for (Participant p : getParticipants()) {
                pList.add(new ResourcingParticipant(p.getID(),
                                 p.getFullName() + " (" + p.getUserID() + ")"));
            }
        }
        catch (IOException ioe) {
            // nothing to do - use empty list
        }
        return pList;
    }


    public static List<ResourcingRole> getResourcingRoles() {
        List<ResourcingRole> rList = new ArrayList<ResourcingRole>();
        try {
            for (Role r : getRoles()) {
                rList.add(new ResourcingRole(r.getID(), r.getName()));
            }
        }
        catch (IOException ioe) {
            // nothing to do - use empty list
        }
        return rList;
    }


    public static List<ResourcingAsset> getResourcingAssets() {
        List<ResourcingAsset> assetList = new ArrayList<ResourcingAsset>();
        try {
            for (NonHumanResource resource : getNonHumanResources()) {
                assetList.add(new ResourcingAsset(resource.getID(), resource.getName()));
            }
            Collections.sort(assetList);
        }
        catch (IOException ioe) {
            // nothing to do - use empty list
        }
        return assetList;
    }


    public static List<ResourcingCategory> getResourcingCategories() {
        List<ResourcingCategory> categories = new ArrayList<ResourcingCategory>();
        try {
            for (NonHumanCategory category : getNonHumanCategories()) {
                String catName = category.getName();
                String catID = category.getID();
                categories.add(new ResourcingCategory(catID, catName));
                for (String subcat : category.getSubCategoryNames()) {
                    categories.add(new ResourcingCategory(catID, catName, subcat));
                }
            }
            Collections.sort(categories);
        }
        catch (IOException ioe) {
            // nothing to do - use empty list
        }
        return categories;
    }


    public static List<AllocationMechanism> getAllocationMechanisms() {
        List<AllocationMechanism> allocators = new ArrayList<AllocationMechanism>();
        try {
            for (AbstractSelector allocator : _resConn.getAllocators()) {
                allocators.add(
                        new AllocationMechanism(
                                allocator.getName(),
                                allocator.getCanonicalName(),
                                allocator.getDisplayName(),
                                allocator.getDescription()
                        )
                );
            }
        } catch (IOException ioe) {
            // nothing to do - use empty list
        }
        return allocators;
    }


    public static List<ResourcingFilter> getResourcingFilters() {
        List<ResourcingFilter> filters = new ArrayList<ResourcingFilter>();
        try {
            for (AbstractSelector filter : _resConn.getFilters()) {
                filters.add(
                        new ResourcingFilter(
                                filter.getName(),
                                filter.getCanonicalName(),
                                filter.getDisplayName(),
                                filter.getParams()
                        ));
            }
        } catch (IOException ioe) {
            // nothing to do - use empty list
        }
        return filters;
    }


    public static List<DataVariable> getCodeletParameters(String codeletName) {
        List<YParameter> params = null;
        try {
            params = _resConn.getCodeletParameters(codeletName);
        }
        catch (IOException e) {
            // fall through to if statement below
        }
        List<DataVariable> varList = new ArrayList<DataVariable>();
        if ((params == null) || params.isEmpty()) return varList;      // no more to do

        varList = mergeParameters(params);

        int index = 0;
        for (DataVariable var : varList) {
            var.setIndex(index++);
            var.setUserDefined(false);
        }
        return varList;
    }


    public static List<DataVariable> getServiceParameters(String serviceURI) {
        List<YParameter> params = null;
        try {
            params = Arrays.asList(_engConn.getParametersForService(serviceURI));
        }
        catch (IOException e) {
            LogWriter.warn(e.getMessage());
        }
        List<DataVariable> varList = new ArrayList<DataVariable>();
        if ((params == null) || params.isEmpty()) return varList;      // no more to do

        varList = mergeParameters(params);

        int index = 0;
        for (DataVariable var : varList) {
            var.setIndex(index++);
        }
        return varList;
    }


    private static List<DataVariable> mergeParameters(List<YParameter> params) {
        List<DataVariable> varList = new ArrayList<DataVariable>();

        // map params to data variables
        for (YParameter param : params) {
            DataVariable editorVariable = new DataVariable();
            editorVariable.setDataType(param.getDataTypeName());
            editorVariable.setName(param.getName());
            editorVariable.setInitialValue(param.getInitialValue());
            editorVariable.setUserDefined(false);
            if (param.isInput()) {
                editorVariable.setUsage(DataVariable.USAGE_INPUT_ONLY);
            }
            if (param.isOutput()) {
                editorVariable.setUsage(DataVariable.USAGE_OUTPUT_ONLY);
            }
            if (param.isOptional()) {
                 editorVariable.setAttribute("optional", "true");
             }

            varList.add(editorVariable);
        }

        // merge matching input & output vars into one I&O var
        List<DataVariable> inputList = new ArrayList<DataVariable>();
        List<DataVariable> outputList = new ArrayList<DataVariable>();
        for (DataVariable variable : varList) {
            if (variable.getUsage() == DataVariable.USAGE_INPUT_ONLY) {
                inputList.add(variable);
            }
            else {
                outputList.add(variable);
            }
        }
        for (DataVariable inputVar : inputList) {
            for (DataVariable outputVar : outputList) {
                if (inputVar.equalsIgnoreUsage(outputVar)) {
                    inputVar.setUsage(DataVariable.USAGE_INPUT_AND_OUTPUT);
                    varList.remove(outputVar);
                }
            }
        }
        return varList;
    }


    private static URL makeURL(String url) {
        if (url != null) {
            try {
                return new URL(url);
            }
            catch (MalformedURLException mue) {
                // fallthrough to null
            }
        }
        return null;
    }

}
