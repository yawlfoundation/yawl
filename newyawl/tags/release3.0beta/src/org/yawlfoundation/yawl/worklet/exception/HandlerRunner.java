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

package org.yawlfoundation.yawl.worklet.exception;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.RdrConclusion;
import org.yawlfoundation.yawl.worklet.rdr.RuleType;
import org.yawlfoundation.yawl.worklet.support.Library;
import org.yawlfoundation.yawl.worklet.support.RdrConversionTools;
import org.yawlfoundation.yawl.worklet.support.WorkletRecord;

import java.util.List;

/** The HandlerRunner class manages an exception handling process. An instance
 *  of this class is created for each exception process raised by a case. The CaseMonitor
 *  class maintains the current set of HandlerRunners for a case (amongst other things).
 *  This class also manages a running worklet instance for a 'parent' case
 *  when required.
 *
 *  The key data member is the RdrConclusion _rdrConc, which contains the
 *  sequential set of exception handling primitives for this particular handler.
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */

public class HandlerRunner extends WorkletRecord {

    private RdrConclusion _rdrConc = null ;         // steps to run for this handler
    private CaseMonitor _parentMonitor = null ;     // case that generated this instance
    private int _actionIndex = 1 ;                  // index to 'primitives' set
    private int _actionCount = 0 ;                  // number of primitives in set
    private boolean _isItemSuspended;               // has excepted item been suspended?
    private boolean _isCaseSuspended;               // has case been suspended?
    private List<WorkItemRecord> _suspendedItems = null; // list of suspended items - can
                                                    // be child items, or for whole case

    private String _rdrConcStr = null ;             // required for persistence
    private String _caseID = null ;
    private int _id ;
    private String _suspList = null ;


   /**
     * This constructor is used when an exception is raised at the case level
     * @param monitor the CaseMonitor for the case the generated the exception
     * @param rdrConc the RdrConclusion of a rule that represents the handling process
     */
    public HandlerRunner(CaseMonitor monitor, RdrConclusion rdrConc, RuleType xType) {
        super() ;
        _parentMonitor = monitor ;
        _rdrConc = rdrConc ;
        _reasonType = xType ;
        _actionCount = _rdrConc.getCount();
        _isItemSuspended = false;
        _isCaseSuspended = false ;
        _searchPair = rdrConc.getLastPair();
        _log = Logger.getLogger(this.getClass());
        initPersistedData() ;
    }

    /** This constructor is used when an exception is raised at the workitem level */
    public HandlerRunner(CaseMonitor monitor, WorkItemRecord wir,
                         RdrConclusion rdrConc, RuleType xType) {
        this(monitor, rdrConc, xType);
        _wir = wir ;
        _wirStr = wir.toXML();
    }

    /** This one's for persistence */
    private HandlerRunner() {}


    //***************************************************************************//

    // GETTERS //

   /** @return the action for the current index from the RdrConclusion */
    public String getNextAction() {
        if (_actionCount < _actionIndex) return null ;
        return _rdrConc.getAction(_actionIndex);
    }


    /** @return the target for the current index from the RdrConclusion */
    public String getNextTarget() {
        if (_actionCount < _actionIndex) return null ;
        return _rdrConc.getTarget(_actionIndex);
    }


    /** @return the current index in the set of actions */
    public int getActionIndex() {
        return _actionIndex ;
    }


     /** @return the id of the case that raised the exception */
    public String getCaseID() {
        return _parentMonitor.getCaseID();
    }

    /** @return the id of the spec of the case that raised the exception */
   public YSpecificationID getSpecID() {
       return _parentMonitor.getSpecID();
   }


    /** @return the CaseMonitor that is the container for this HandlerRunner */
    public CaseMonitor getOwnerCaseMonitor() {
        return _parentMonitor ;
    }


    /** @return the list of currently suspended workitems for this runner */
    public List<WorkItemRecord> getSuspendedList() {
       return _suspendedItems ;
    }


    /** @return the data params for the parent workitem/case */
    public Element getDatalist() {
        Element list = super.getDatalist() ;
        if (list == null) list = _parentMonitor.getCaseData();
        return list ;
    }

    public Element getUpdatedData() {
        if (_wir != null) return _wir.getUpdatedData();
        return _parentMonitor.getCaseData();
    }

    //***************************************************************************//

    // SETTERS //

    /** called when an action suspends the workitem of this HandlerRunner */
    public void setItemSuspended() {
        _isItemSuspended = true ;
        persistThis();
    }


    /** called when an action unsuspends the workitem of this HandlerRunner */
    public void unsetItemSuspended() {
        _isItemSuspended = false ;
        unsetSuspendedList();
    }

    /** called when an action suspends the case of this HandlerRunner */
    public void setCaseSuspended() {
        _isCaseSuspended = true ;
        persistThis();
    }


    /** called when an action unsuspends the case of this HandlerRunner */
    public void unsetCaseSuspended() {
        _isCaseSuspended = false ;
        unsetSuspendedList();
    }


    public void setOwnerCaseMonitor(CaseMonitor monitor) {
         _parentMonitor = monitor ;
     }


     /** called when an action suspends the item or parent case of this HandlerRunner */
    public void setSuspendedList(List<WorkItemRecord> items) {
        _suspendedItems = items ;
        _suspList = RdrConversionTools.WIRListToString(items);
         persistThis();
    }


    /** called when an action unsuspends the item or parent case of this HandlerRunner */
    public void unsetSuspendedList() {
        _suspendedItems = null ;
        _suspList = null;
        persistThis();
    }


    //***************************************************************************//

    // PERSISTENCE METHODS //

    private int get_actionIndex() { return _actionIndex ; }

    private int get_actionCount() { return _actionCount ; }

    private boolean get_isItemSuspended() { return _isItemSuspended ; }

    private boolean get_isCaseSuspended() { return _isCaseSuspended ; }

    private String get_rdrConcStr() { return _rdrConcStr ; }

    private String get_caseID() { return _caseID ; }

    private String get_suspList() { return _suspList ; }

    public int get_id() { return _id ; }

    private void set_actionIndex(int i) { _actionIndex = i; }

    private void set_actionCount(int i) { _actionCount = i ; }

    private void set_isItemSuspended(boolean b) { _isItemSuspended = b; }

    private void set_isCaseSuspended(boolean b) { _isCaseSuspended = b; }

    private void set_rdrConcStr(String s) { _rdrConcStr = s; }

    private void set_caseID(String s) { _caseID = s; }

    private void set_suspList(String s) { _suspList = s; }

    public void set_id(int id) { _id = id ; }


    /** Stringifies some data members for persistence purposes */
    private void initPersistedData() {
        _rdrConcStr = JDOMUtil.elementToStringDump(_rdrConc.getConclusion());
        _caseID = _parentMonitor.getCaseID() ;
        _id = this.hashCode();
    }


    /** re-converts stringified persisted data back to data members after restore */
    public void initNonPersistedItems() {
        _rdrConc = new RdrConclusion(JDOMUtil.stringToElement(_rdrConcStr));

        if (_wirStr != null) {                                      // if item runner
            _wir = RdrConversionTools.xmlStringtoWIR(_wirStr);
            _datalist = _wir.getWorkItemData();
        }

        // reconstitute the susp items list
        if (_suspList != null) {
            _suspendedItems = RdrConversionTools.xmlToWIRList(_suspList);
        }
    }


    //***************************************************************************//

    // MISC //


    public int incActionIndex() {
        ++_actionIndex;
        persistThis();
        return _actionIndex;
    }

     /** @return true if there is another action to run in the set */
    public boolean hasNextAction() {
        return (_actionIndex <= _actionCount);
    }

    public boolean isItemSuspended() {
        return _isItemSuspended;
    }

    public boolean isCaseSuspended() {
        return _isCaseSuspended;
    }

    public String toString() {
        StringBuilder s = new StringBuilder("HandlerRunner record:") ;
        s.append(Library.newline);
        s.append(super.toString());

        String conc = (_rdrConc == null) ? "null" : _rdrConc.toString();
        String parent = (_parentMonitor == null)? "null" :
                         _parentMonitor.getSpecID() + ": " + _parentMonitor.getCaseID();
        String index = String.valueOf(_actionIndex);
        String count = String.valueOf(_actionCount);
        String itemSusp = String.valueOf(_isItemSuspended);
        String caseSusp = String.valueOf(_isCaseSuspended);
        String wirs = "";
        if (_suspendedItems != null) {
            for (WorkItemRecord wir : _suspendedItems) {
               wirs += wir.toXML() + Library.newline ;
            }
        }

        s = Library.appendLine(s, "RDRConclusion", conc);
        s = Library.appendLine(s, "Parent Monitor", parent);
        s = Library.appendLine(s, "Action Index", index);
        s = Library.appendLine(s, "Action Count", count);
        s = Library.appendLine(s, "Item Suspended?", itemSusp);
        s = Library.appendLine(s, "Case Suspended?", caseSusp);
        s = Library.appendLine(s, "Suspended Items", wirs);

        return s.toString();
    }


        /**
     * writes the node id's for the nodes returned from the rdr search
     * and the data for the current workitem, to a file for later
     * input into the 'add rule' process, if required
     */
    public void saveSearchResults() {

        // create the required components for the output file
        Document doc = new Document(new Element("searchResult")) ;
        Element eLastNode = new Element("lastNode") ;
        Element eSatisfied = new Element("satisfied") ;
        Element eTested = new Element("tested") ;
        Element eId = new Element("id") ;
        Element eSpecid = new Element("specid") ;
        Element eSpecVersion = new Element("specversion") ;
        Element eSpecURI = new Element("specuri") ;
        Element eTaskid = new Element("taskid") ;
        Element eCaseid = new Element("caseid") ;
        Element eCaseData = new Element("casedata") ;
        Element eReason = new Element("extype") ;
        Element eWorklets = new Element("worklets") ;

        try {
            // transfer the workitem's data items to the file
            for (Element e : getDatalist().getChildren()) {
                eCaseData.addContent(e.clone());
            }

           // set values for case identifiers
           eSpecid.setText(_parentMonitor.getSpecID().getIdentifier());
           eSpecVersion.setText(_parentMonitor.getSpecID().getVersionAsString());
           eSpecURI.setText(_parentMonitor.getSpecID().getUri());
           eCaseid.setText(_parentMonitor.getCaseID());

           if (_wir != null) {
               // set values for the workitem identifiers (item-level exception)
               eId.setText(_wir.getID()) ;
               eTaskid.setText(Library.getTaskNameFromId(_wir.getTaskID()));
            }

            // add the worklet names and case ids
            for (String wName : _runners.getAllWorkletNames()) {
                Element eWorkletName = new Element("workletName");
                Element eRunningCaseId = new Element("runningcaseid");
                Element eWorklet = new Element("worklet");
                eWorkletName.setText(wName);
                eRunningCaseId.setText(_runners.getCaseID(wName));
                eWorklet.addContent(eWorkletName);
                eWorklet.addContent(eRunningCaseId);
                eWorklets.addContent(eWorklet);
            }

            eReason.setText(String.valueOf(_reasonType));

            // add the nodeids to the relevent elements
            eSatisfied.setText(_searchPair[0].getNodeIdAsString()) ;
            eTested.setText(_searchPair[1].getNodeIdAsString()) ;
            eLastNode.addContent(eSatisfied) ;
            eLastNode.addContent(eTested) ;

            // add the elements to the document
            Element root = doc.getRootElement();
            root.addContent(eId) ;
            root.addContent(eSpecid);
            root.addContent(eSpecVersion);
            root.addContent(eSpecURI);
            root.addContent(eTaskid);
            root.addContent(eCaseid);
            root.addContent(eWorklets) ;
            root.addContent(eReason);
            root.addContent(eLastNode) ;
            root.addContent(eCaseData) ;

            // create the output file
            saveDocument(createFileName(), doc) ;
        }
        catch (IllegalAddException iae) {
            _log.error("Exception when adding content", iae) ;
        }
     }


}  // end HandlerRunner class


