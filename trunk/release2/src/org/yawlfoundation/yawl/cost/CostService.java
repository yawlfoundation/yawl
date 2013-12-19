/*
 * Copyright (c) 2004-2012 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.cost;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.jdom2.Element;
import org.yawlfoundation.yawl.cost.data.CostModel;
import org.yawlfoundation.yawl.cost.data.CostModelCache;
import org.yawlfoundation.yawl.cost.data.HibernateEngine;
import org.yawlfoundation.yawl.cost.evaluate.Predicate;
import org.yawlfoundation.yawl.cost.evaluate.PredicateEvaluator;
import org.yawlfoundation.yawl.cost.log.Annotator;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_Service;
import org.yawlfoundation.yawl.engine.interfce.interfaceX.InterfaceX_ServiceSideClient;
import org.yawlfoundation.yawl.resourcing.datastore.eventlog.ResourceEvent;
import org.yawlfoundation.yawl.resourcing.resource.Participant;
import org.yawlfoundation.yawl.resourcing.resource.Role;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceGatewayClient;
import org.yawlfoundation.yawl.resourcing.rsInterface.ResourceLogGatewayClient;
import org.yawlfoundation.yawl.schema.SchemaHandler;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.util.XNode;
import org.yawlfoundation.yawl.util.XNodeParser;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michael Adams
 * @date 3/10/11
 */
public class CostService implements InterfaceX_Service {

    private Map<YSpecificationID, CostModelCache> _models;
    private HibernateEngine _dataEngine;
    private PredicateEvaluator _evaluator;
    private InterfaceX_ServiceSideClient _ixClient;    // interface client to engine
    private ResourceLogGatewayClient _rsLogClient;
    private ResourceGatewayClient _rsOrgDataClient;
    private String _rsHandle = null;
    private String _engineLogonName;
    private String _engineLogonPassword;
    private static CostService INSTANCE;

    private Logger _log = Logger.getLogger(this.getClass());

    private CostService() {
        _models = new ConcurrentHashMap<YSpecificationID, CostModelCache>();
        _evaluator = new PredicateEvaluator();
        _dataEngine = HibernateEngine.getInstance(true);
        restore();
    }


    public static CostService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CostService();
        }
        return INSTANCE;
    }


    public void shutdown() {
        if (_dataEngine != null) _dataEngine.closeFactory();
    }


    public void setInterfaceXBackend(String uri) {
        _ixClient = new InterfaceX_ServiceSideClient(uri);
    }

    public void setResourceLogURI(String uri) {
        _rsLogClient = new ResourceLogGatewayClient(uri);
    }

    public void setResourceOrgDataURI(String uri) {
        _rsOrgDataClient = new ResourceGatewayClient(uri);
    }

    public void setEngineLogonName(String name) {
        _engineLogonName = name;
    }

    public void setEngineLogonPassword(String password) {
        _engineLogonPassword = password;
    }

    public String importModel(String costModel) {
        return importModel(new XNodeParser(false).parse(costModel));
    }

    public String importModels(String costModels) {
        return importModels(new XNodeParser(false).parse(costModels));
    }


    public String importModel(XNode costModel) {
        if (isValidModel(costModel)) {
            addToCache(new CostModel(costModel), false, true);
            return "<SUCCESS/>";
        } else return failMsg("Invalid model - check logs for details.");
    }


    public String importModels(XNode costModels) {
        if (costModels != null) {
            int additions = 0;
            int modelCount = costModels.getChildCount();
            for (XNode costModel : costModels.getChildren("costmodel")) {
                if (isValidModel(costModel)) {
                    addToCache(new CostModel(costModel), false, false);
                    additions++;
                }
            }
            if (additions > 0) {
                _dataEngine.commit();
                return successMsg("Successfully added " + additions + " out of " +
                        modelCount + " model(s).");
            } else {
                return failMsg("Import failed: 0 out of " + modelCount + " valid models.");
            }
        } else return failMsg("Malformed XML - check logs for details.");
    }


    public String exportModels(YSpecificationID specID) {
        Set<CostModel> models = getModels(specID);
        if (models == null) {
            return failMsg("No models found for specification.");
        }
        XNode node = new XNode("costmodels");
        for (CostModel model : models) {
            node.addChild(model.toXNode());
        }
        return node.toPrettyString();
    }


    public String exportModel(YSpecificationID specID, String modelID) {
        CostModel model = getModel(specID, modelID);
        if (model == null) {
            return failMsg("Model not found.");
        }
        return model.toXML();
    }


    public CostModelCache getModelCache(YSpecificationID specID) {
        return _models.get(specID);
    }


    public CostModelCache getModelCache(String specURI, String version) {
        for (YSpecificationID specID : _models.keySet()) {
            if (specID.getUri().equals(specURI) &&
                    specID.getVersionAsString().equals(version)) {
                return getModelCache(specID);
            }
        }
        return null;
    }


    public Set<CostModel> getModels(YSpecificationID specID) {
        CostModelCache cache = getModelCache(specID);
        return (cache != null) ? cache.getModels() : null;
    }


    public CostModel getModel(YSpecificationID specID, String modelID) {
        CostModelCache cache = getModelCache(specID);
        return (cache != null) ? cache.getModel(modelID) : null;
    }


    public String removeModel(YSpecificationID specID, String modelID) {
        return removeModel(getModel(specID, modelID));
    }

    public String clearModels(YSpecificationID specID) {
        Set<CostModel> models = getModels(specID);
        if (models != null) {
            for (CostModel model : models) {
                _dataEngine.exec(model, HibernateEngine.DB_DELETE, false);
            }
            _dataEngine.commit();

            int removed = models.size();
            _models.get(specID).clear();
            return successMsg("Successfully removed " + removed + " model(s).");
        } else return failMsg("No models found for specification.");
    }


    public String removeModel(CostModel model) {
        if (model != null) {
            return removeModel(model, _models.get(model.getSpecID()));
        }
        return failMsg("Attempt to remove a null cost model.");
    }


    public String removeModel(CostModel model, CostModelCache cache) {
        return removeModel(model, cache, true);
    }

    private String removeModel(CostModel model, CostModelCache cache, boolean commit) {
        boolean removed = (cache != null) && cache.remove(model);
        if (removed) {
            _dataEngine.exec(model, HibernateEngine.DB_DELETE, commit);
        }
        return removed ? successMsg("Successfully removed model: " + model.getId()) :
                failMsg("Failed to remove model" + model.getId());
    }


    public Set<CostModel> getModels(String specURI, String version) {
        CostModelCache cache = getModelCache(specURI, version);
        return (cache != null) ? cache.getModels() : null;
    }


    public String getAnnotatedLog(YSpecificationID specID, boolean withData) {
        if (!_models.containsKey(specID)) {
            return failMsg("No cost models matching specification ID");
        }
        try {
            String log = _rsLogClient.getMergedXESLog(specID.getIdentifier(),
                    specID.getVersionAsString(), specID.getUri(), withData, getRSHandle());
            if (log == null) throw new IOException();
            Annotator annotator = new Annotator(log);
            annotator.setSpecID(specID);
            return annotator.annotate();
        } catch (IOException ioe) {
            return failMsg("Could not get base log from resource service");
        } catch (IllegalStateException ise) {
            return failMsg(ise.getMessage());
        }
    }


    public boolean evaluate(YSpecificationID specID, String caseID, String predicate) {
        try {

            // verify the predicate
            Predicate costPredicate = new Predicate(predicate);

            // if verified (no exception), get the list of events for the predicate
            List<ResourceEvent> eventList = getLogEvents(specID, caseID, costPredicate);

            // get cost model(s) and evaluate
            CostModelCache cache = getModelCache(specID);
            if (cache != null) {
                return _evaluator.evaluate(costPredicate, eventList, cache.getDriverMatrix());
            }
            else throw new IllegalArgumentException("No cost models found for case " + caseID);
        }
        catch (Exception e) {
            _log.error("Failed to evaluate cost predicate: ", e);
        }
        return false;
    }


    /**
     * Resolves an id to a set of participant ids
     *
     * @param resourceID can be the id of a Participant, Role, Position, Capability or
     *                   OrgGroup
     * @return the set of Participant ID's referenced
     */
    public Set<String> resolveResources(String resourceID) {
        Set<String> resources = new HashSet<String>();
        if (resourceID == null) return resources;
        try {
            String xml = _rsOrgDataClient.getReferencedParticipantIDs(
                    resourceID, getRSHandle());
            if (xml != null) {
                XNode node = new XNodeParser().parse(xml);
                if (node != null) {
                    for (XNode idNode : node.getChildren()) {
                        resources.add(idNode.getText());
                    }
                }
            }
        } catch (IOException ioe) {
            // nothing to do;
        }
        return resources;
    }

    /**
     * *****************************************************************************
     */

    private String getRSHandle() throws IOException {
        if (_rsHandle == null ||
                ! _rsLogClient.checkConnection(_rsHandle).equalsIgnoreCase("true")) {
            _rsHandle = _rsLogClient.connect(_engineLogonName, _engineLogonPassword);
            if (_rsHandle.startsWith("<fail")) throw new IOException();
        }
        return _rsHandle;
    }


    private String failMsg(String msg) {
        return "<failure>" + msg + "</failure>";
    }

    private String successMsg(String msg) {
        return "<success>" + msg + "</success>";
    }


    private boolean isValidModel(XNode costModel) {
        if (costModel == null) return false;
        URL schema = getClass().getResource("/org/yawlfoundation/yawl/cost/xsd/costmodel.xsd");
        SchemaHandler validator = new SchemaHandler(schema);
        boolean valid = validator.compileAndValidate(costModel.toString());
        if (!valid) _log.error(validator.getConcatenatedMessage());
        return valid;
    }


    private CostModelCache addToCache(CostModel model, boolean restoring, boolean commit) {
        if (model == null) return null;
        YSpecificationID specID = model.getSpecID();
        CostModelCache cache = _models.get(specID);
        if (cache == null) {
            cache = new CostModelCache(specID);
            _models.put(specID, cache);
        }
        removePrevVersion(cache, model);      // if any
        if (cache.add(model) && (!restoring)) {
            _dataEngine.exec(model, HibernateEngine.DB_INSERT, commit);
        }
        return cache;
    }


    private void removePrevVersion(CostModelCache cache, CostModel model) {
        CostModel oldModel = cache.getModel(model.getId());   // get prev version if any
        if (oldModel != null) {
            removeModel(oldModel, cache, false);
        }
    }


    private void restore() {
        List objects = _dataEngine.getObjectsForClass("CostModel");
        if (objects != null) {
            for (Object o : objects) {
                CostModel model = (CostModel) o;
                Hibernate.initialize(model.getDrivers());
                addToCache(model, true, false);
            }
            _dataEngine.commit();
        }
    }


    private List<ResourceEvent> getLogEvents(YSpecificationID specID, String caseID,
                                             Predicate predicate) throws IOException {
        List<ResourceEvent> eventList = new ArrayList<ResourceEvent>();
        if (predicate.isAllCases()) {
            appendEvents(eventList,
                    _rsLogClient.getSpecificationEvents(specID, getRSHandle()));
        }
        else if (predicate.hasCaseList()) {
            for (String rangeID : predicate.getCaseList()) {
                appendEvents(eventList, _rsLogClient.getCaseEvents(rangeID, getRSHandle()));
            }
        }
        else {
            appendEvents(eventList, _rsLogClient.getCaseEvents(caseID, getRSHandle()));
        }
        if (eventList.isEmpty()) {
            throw new IllegalArgumentException("No events found for case " + caseID);
        }
        return eventList;
    }


    private List<ResourceEvent> parseEventLog(String log) {
        List<ResourceEvent> eventList = new ArrayList<ResourceEvent>();
        if (! (log == null || log.startsWith("<fail"))) {
            Element events = JDOMUtil.stringToElement(log);
            if (events != null) {
                for (Element event : events.getChildren()) {
                    eventList.add(new ResourceEvent(event));
                }
            }
        }
        return eventList;
    }


    private void appendEvents(List<ResourceEvent> eventList, String log) {
        eventList.addAll(parseEventLog(log));
    }

    /**
     * ****************************************************************************
     */

    // Implemented Interface Methods //
    public void handleCheckCaseConstraintEvent(YSpecificationID specID, String caseID,
                                               String data, boolean precheck) {
        //        CostModelCache cache = _models.get(specID);
        //        if (cache != null) {
        //            if (precheck) {
        //                logCaseStart(cache, caseID, data);
        //            }
        //            else {
        //                logCaseEnd(cache, caseID, data);
        //            }
        //        }
    }

    public void handleCheckWorkItemConstraintEvent(WorkItemRecord wir, String data,
                                                   boolean precheck) {
        //        CostModelCache cache = _models.get(new YSpecificationID(wir));
        //        if (cache != null) {
        //            if (precheck) {
        //                logItemStart(cache, wir, data);
        //            }
        //            else {
        // //               logItemEnd(cache, wir, data);
        //            }
        //        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        response.setContentType("text/html");
        PrintWriter outputWriter = response.getWriter();
        outputWriter.write(
                "<html><head><title>YAWL Cost Service</title></head><body>" +
                        "<H3>Welcome to the YAWL Cost Service</H3></body></html>");
        outputWriter.flush();
        outputWriter.close();
    }


    /**
     * ************************************************************************************
     */

    // Unimplemented Methods //
    public String handleWorkItemAbortException(WorkItemRecord wir, String caseData) {
        return null;
    }

    public void handleTimeoutEvent(WorkItemRecord wir, String taskList) { }

    public String handleConstraintViolationException(WorkItemRecord wir, String caseData) {
        return null;
    }

    public void handleCaseCancellationEvent(String caseID) { }

    public void handleResourceUnavailableException(String resourceID, WorkItemRecord wir,
                                                   String caseData, boolean primary) { }

}
