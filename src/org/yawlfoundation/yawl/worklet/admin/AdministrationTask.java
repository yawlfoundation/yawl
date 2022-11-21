/*
 * Copyright (c) 2004-2020 The YAWL Foundation. All rights reserved.
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

package org.yawlfoundation.yawl.worklet.admin;

import org.apache.logging.log4j.LogManager;
import org.yawlfoundation.yawl.util.StringUtil;
import org.yawlfoundation.yawl.util.XNode;

/**
 *  This class stores the details of a pending administration task, viewed via
 *  the worklet admin screens.
 *
 *  The AdminTasksManager maintains a set of these AdministrationTask instances.
 *
 *  @author Michael Adams
 *  @version 0.8, 04-09/2006
 */

public class AdministrationTask {

    // data describing current case/item/worklet
    private String _caseID;
    private String _itemID ;
    private int _wsTaskID ;
    private int _taskType ;

    // user-defined descriptions of proposed task
    private String _title ;
    private String _scenario ;
    private String _process ;

    // task types
    public static final int TASKTYPE_REJECTED_SELECTION = 0;
    public static final int TASKTYPE_CASE_EXTERNAL_EXCEPTION = 1;
    public static final int TASKTYPE_ITEM_EXTERNAL_EXCEPTION = 2;


    public AdministrationTask() {}                        // required for persistence

    /** the constructors */
    public AdministrationTask(String caseID, String title, String scenario,
                              String process, int taskType) {
        _caseID = caseID ;
        _title = title ;
        _scenario = scenario ;
        _process = process ;
        _taskType = taskType ;
    }

    /** for item-level task */
    public AdministrationTask(String caseID, String itemID, String title, String scenario,
                              String process, int taskType) {
        this(caseID, title, scenario, process, taskType);
        _itemID = itemID;
    }


    public String toXML() {
        XNode node = new XNode("task");
        node.addChild("id", getID());
        node.addChild("caseid", getCaseID());
        if (_itemID != null) {
            node.addChild("itemid", getItemID());
        }
        node.addChild("type", getTaskType());
        node.addChild("title", getTitle(), true);
        node.addChild("scenario", getScenario(), true);
        node.addChild("process", getProcess(), true);
        return node.toString();
    }


    public void fromXNode(XNode node) {
        if (node != null) {
            setID(StringUtil.strToInt(node.getChildText("caseID"), -1));
            setCaseID(node.getChildText("caseid"));
            setItemID(node.getChildText("itemid"));
            setTaskType(StringUtil.strToInt(node.getChildText("type"), 0));
            setTitle(node.getChildText("title", true));
            setScenario(node.getChildText("scenario", true));
            setProcess(node.getChildText("process", true));
        }
    }

    /***************************************************************************/

    // SETTERS & GETTERS //

    public String getCaseID() {
        return _caseID ;
    }

    public String getItemID() {
        return _itemID ;
    }

    public int getID() {
        return _wsTaskID ;
    }

    public int getTaskType() {
        return _taskType ;
    }

    public String getTitle() {
        return _title ;
    }

    public String getScenario() {
        return _scenario ;
    }

    public String getProcess() {
        return _process ;
    }


    public void setCaseID(String caseID) {
        _caseID = caseID ;
    }

    public void setItemID(String itemID) {
        _itemID = itemID;
    }

    public void setID(int id) { _wsTaskID = id ; }

    public void setTaskType(int taskType) {
        if ((taskType >= 0) && (taskType <= 1))
           _taskType = taskType ;
        else
            LogManager.getLogger(AdministrationTask.class).error(
                    "Unable to set task type - invalid type identifier");
    }

    public void setTitle(String title) {
        _title = title;
    }

    public void setScenario(String scenario) {
        _scenario = scenario ;
    }

    public void setProcess(String process) {
        _process = process ;
    }

    /*******************************************************************************/

    public String toString() {
        StringBuilder s = new StringBuilder() ;
        String ttype = null ;

        if (_taskType == TASKTYPE_REJECTED_SELECTION)
            ttype = "Rejected Selection";
        else if (_taskType == TASKTYPE_CASE_EXTERNAL_EXCEPTION)
            ttype = "New Case-Level External Exception";
        else if (_taskType == TASKTYPE_ITEM_EXTERNAL_EXCEPTION)
            ttype = "New Item-Level External Exception";

        return "ID: " + ttype + "; CASE ID: " + _caseID;
    }

    /********************************************************************************/

    // ACCESSORS & MUTATORS FOR PERSISTENCE //

    public String get_caseID() { return _caseID ; }

    public String get_itemID() { return _itemID ; }

    public int get_wsTaskID() { return _wsTaskID ; }

    public int get_taskType() { return _taskType ; }

    public String get_title() { return _title; }

    public String get_scenario() { return _scenario ; }

    public String get_process() { return  _process ; }

    public void set_caseID(String s) { _caseID = s ; }

    public void set_itemID(String s) { _itemID = s ; }

    public void set_wsTaskID(int i) { _wsTaskID = i; }

    public void set_taskType(int i) {  _taskType = i ; }

    public void set_title(String s) { _title = s; }

    public void set_scenario(String s) { _scenario = s ; }

    public void set_process(String s) { _process = s ; }

    /*********************************************************************************/
}
