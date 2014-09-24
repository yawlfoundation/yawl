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

package org.yawlfoundation.yawl.worklet.support;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalAddException;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.yawlfoundation.yawl.engine.YSpecificationID;
import org.yawlfoundation.yawl.engine.interfce.WorkItemRecord;
import org.yawlfoundation.yawl.util.JDOMUtil;
import org.yawlfoundation.yawl.worklet.rdr.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** The WorkletRecord class maintains a generic dataset for derived classes
 *  that manage a currently running worklet for a 'parent' process.
 *
 * It is implemented by selection.CheckedOutChildItem and exception.HandlerRunner
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */


public class WorkletRecord {

    protected CaseMap _runners = new CaseMap() ;  // executing caseid <==> name mapping
    protected WorkItemRecord _wir ;          // the child workitem
    protected Element _datalist ;            // data passed to this workitem
    protected RdrPair _searchPair ;        // rule pair returned from search
    protected RuleType _reasonType ;         // why the worklet was raised
    protected static Logger _log ;           // log file for debug messages

    protected String _persistID ;            // unique id field for persistence
    protected boolean _hasPersisted = false; // set to true when row is inserted
    protected String _searchPairStr ;        // intermediate str for persistence
    protected String _wirStr ;               // intermediate str for persistence
    protected String _runningCaseIdStr ;     // intermediate str for persistence
    protected String _runningWorkletStr ;     // intermediate str for persistence


    /**
     *  Constructs a basic WorkletRecord
     */
    public WorkletRecord() {}

//===========================================================================//

    // SETTERS //

    public void setItem(WorkItemRecord w) {
        _wir = w ;
        if (_hasPersisted) persistThis();
    }


    public void setDatalist(Element d) {
        _datalist = d ;
    }


    public void addRunner(String caseID, String wName) {
        _runners.addCase(caseID, wName);
        if (_hasPersisted) persistThis();
    }


    public void setSearchPair(RdrPair pair) {
        _searchPair = pair ;
        if (_hasPersisted) persistThis();
    }


    public void setExType(RuleType xType) {
        _reasonType = xType;
        if (_hasPersisted) persistThis();
    }


    public void removeRunnerByCaseID(String caseID) {
        _runners.removeCase(caseID) ;
        if (_hasPersisted) persistThis();
    }

    public void removeRunnerByWorkletName(String wName) {
        _runners.removeWorklet(wName);
        if (_hasPersisted) persistThis();
    }

    public void removeAllCases() {
        _runners.removeAllCases();
    }


 //===========================================================================//

    //*** GETTERS ***//

    public String getWorkletName(String caseID) {
        return _runners.getWorkletName(caseID) ;
    }

    public String getWorkletCaseID(String wName) {
        return _runners.getCaseID(wName) ;
    }

    public Set<String> getWorkletList() {
        return _runners.getAllWorkletNames() ;
    }

    public Map<String, String> getCaseMapAsCSVList() {
        return _runners.getCaseMapAsCSVLists();
    }


     public WorkItemRecord getItem() {
        return _wir ;
    }


    public String getItemId() {
        return _wir.getID() ;
    }


    public RdrPair getSearchPair() {
        return _searchPair ;
    }


    public Set<String> getRunningCaseIds() {
        return _runners.getAllCaseIDs() ;                      // the worklet case ids
    }


    public Element getDatalist() {
        return _datalist ;
    }

    public Element getSearchData() {
        Element processData = _wir.getDataList().clone();

        //convert the wir contents to an Element
        Element wirElement = JDOMUtil.stringToElement(_wir.toXML()).detach();

        Element eInfo = new Element("process_info");     // new Element for process data
        eInfo.addContent(wirElement);
        processData.addContent(eInfo);                     // add element to case data
        return processData;
    }

    public String getCaseID() {
        return _wir.getCaseID();            // the originating workitem's case id
    }

    public YSpecificationID getSpecID() {
        return new YSpecificationID(_wir);      // i.e. of the originating workitem
    }

    public RuleType getReasonType() {
        return _reasonType ;
    }

    public boolean hasRunningWorklet() {
        return _runners.hasRunningWorklets();
    }

//===========================================================================//

    // ACCESSORS & MUTATORS FOR PERSISTENCE //

    protected void set_wirStr(String s) {
        _wirStr = s;
    }

    public String get_wirStr() {
        if (_wirStr == null)

            // only if there's a workitem for this record, create its string
            if (_wir != null) _wirStr = _wir.toXML() ;
        return _wirStr ;
    }


    protected void set_runningCaseIdStr(String s) {
        _runningCaseIdStr = s;
    }

    protected String get_runningCaseIdStr() {
        if (_runners != null) {
            Map<String, String> cases = _runners.getCaseMapAsCSVLists();
            _runningCaseIdStr = cases.get("caseIDs");
        }
         return _runningCaseIdStr ;
    }


    protected void set_runningWorkletStr(String s) {
        _runningWorkletStr = s ;
    }

    protected void set_reasonType(int i) {
        _reasonType = RuleType.values()[i] ;
    }

    protected String get_runningWorkletStr() {
        if (_runners != null) {
            Map<String, String> cases = _runners.getCaseMapAsCSVLists();
            _runningWorkletStr = cases.get("workletNames");
        }
        return _runningWorkletStr ;
    }

    protected int get_reasonType() {
        return _reasonType.ordinal() ;
    }


    protected void set_searchPairStr(String s) {
        _searchPairStr = s;
    }

    public String get_searchPairStr() {
        if (_searchPair != null) _searchPairStr = _searchPair.toString();
        return _searchPairStr ;
    }


    private String SearchPairToString(RdrNode[] pair) {
        String result = "";
        if (pair[0] != null) {
            result = pair[0].toXML() + ":::" + pair[1].toXML();
        }
        return result ;
    }


    public void rebuildSearchPair(YSpecificationID specID, String taskID) {

        RdrSet ruleSet = new RdrSetLoader().load(specID);                  // make a new set
        RdrTree tree = ruleSet.getTree(_reasonType, taskID);
        if (tree != null)
            _searchPair = RdrConversionTools.stringToSearchPair(_searchPairStr, tree);
    }

    public void restoreCaseMap() {
        _runners.restore(_runningCaseIdStr, _runningWorkletStr) ;
    }

    public void ObjectPersisted() {
        _hasPersisted = true ;
    }


    protected void persistThis() {
        Persister.getInstance().update(this);
    }


//===========================================================================//

    //*** SAVE METHODS **//

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
            for (Element dataItem : _datalist.getChildren()) {
                eCaseData.addContent(dataItem.clone());
            }

            //set values for the workitem identifiers
            eId.setText(_wir.getID()) ;
            eSpecid.setText(_wir.getSpecIdentifier());
            eSpecVersion.setText(_wir.getSpecVersion());
            eSpecURI.setText(_wir.getSpecURI());
            eTaskid.setText(_wir.getTaskName());
            eCaseid.setText(_wir.getCaseID());
            eReason.setText(String.valueOf(_reasonType));

            // add the worklet names and case ids
            for (String wName : _runners.getAllWorkletNames()) {
                Element eWorkletName = new Element("workletName") ;
                Element eRunningCaseId = new Element("runningcaseid") ;
                Element eWorklet = new Element("worklet");
                eWorkletName.setText(wName) ;
                eRunningCaseId.setText(_runners.getCaseID(wName)) ;
                eWorklet.addContent(eWorkletName);
                eWorklet.addContent(eRunningCaseId);
                eWorklets.addContent(eWorklet);
            }

            // add the nodeids to the relevent elements
            eSatisfied.setText(_searchPair.getLastTrueNode().getNodeIdAsString()) ;
            eTested.setText(_searchPair.getLastEvaluatedNode().getNodeIdAsString()) ;
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
            WorkletRecord._log.error("Exception when adding content", iae) ;
        }
     }


     /** saves a JDOM Document to a file */
     protected void saveDocument(String fileName, Document doc)   {
        try {
           FileOutputStream fos = new FileOutputStream(fileName);
           XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
           xop.output(doc, fos);
           fos.flush();
           fos.close();
      }
      catch (IOException ioe){
          _log.error("IO Exception in saving Document to file", ioe) ;
      }
   }

    protected String createFileName() {
        // build file name from workitem and worklet identifiers
        String extn = ".xws";
        String counter = "" ;
        int i = 0 ;
        StringBuilder fName = new StringBuilder(Library.wsSelectedDir) ;
        fName.append(getCaseID()) ;                         // begin with caseID
        fName.append("_") ;
        fName.append(getSpecID()) ;                         // then spec id
        fName.append("_") ;
        fName.append(_reasonType.toString());

        // if item-level, add the task name also
        if (_wir != null) {
            fName.append("_");
            fName.append(_wir.getTaskID());
        }

        String result = fName.toString();
        while (Library.fileExists(result + counter + extn)) {
            counter = "_" + ++i;
        }

        return (result + counter + extn);
    }

//===========================================================================//

    /** returns a String representation of current WorkletRecord */
    public String toString() {
        StringBuilder s = new StringBuilder("##### WORKLET RECORD #####") ;
        s.append(Library.newline);
        s.append(toStringSub());

        return s.toString() ;
    }

    public String toStringSub() {
        StringBuilder s = new StringBuilder() ;
        String wirStr = (_wir != null)? _wir.toXML() : "null";
        Library.appendLine(s, "WORKITEM", wirStr);

        String data = JDOMUtil.elementToStringDump(_datalist);
        Library.appendLine(s, "DATALIST", data);

        Library.appendLine(s, "WORKLET NAMES", _runners.getWorkletCSVList());
        Library.appendLine(s, "RUNNING CASE IDs", _runners.getCaseIdCSVList());

        s.append("SEARCH PAIR: ");
        if (_searchPair != null) {
            s.append(Library.newline);
            Library.appendLine(s, "Last Satisfied Node", _searchPair.getLastTrueNode().toXML());
            Library.appendLine(s, "Last Tested Node", _searchPair.getLastEvaluatedNode().toXML());
        }
        else s.append("null");

        s.append(Library.newline);
        return s.toString() ;
    }

//===========================================================================//
//===========================================================================//

}
