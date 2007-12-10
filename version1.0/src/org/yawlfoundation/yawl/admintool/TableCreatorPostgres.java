/*
 * This file is made available under the terms of the LGPL licence.
 * This licence can be retrieved from http://www.gnu.org/copyleft/lesser.html.
 * The source remains the property of the YAWL Foundation.  The YAWL Foundation is a collaboration of
 * individuals and organisations who are committed to improving workflow technology.
 *
 */


package org.yawlfoundation.yawl.admintool;

import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class TableCreatorPostgres {
    public static void main(String[] args) throws ClassNotFoundException {
        if (args.length != 3){
            System.out.println("USAGE: java " +
                    "-cp .;postgresql-8.0-311.jdbc3.jar PostgreSQLRunner " +
                    "<DBname> <username> <password>");
            System.exit(1);
        }

        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://localhost/" + args[0];
        Properties props = new Properties();
        props.setProperty("user",args[1]);
        props.setProperty("password",args[2]);
        try{
        Connection conn = DriverManager.getConnection(url, props);
        Statement statement = conn.createStatement();

        createTables(statement);

        conn.close();

        System.out.println("Tables succesfully created in DB: " + args[0] );
        }catch(SQLException e){
        	e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public static void createTables(Statement statement) throws SQLException {


        statement.executeUpdate(
        		"create table Holidays (" +
                "OrgGroupName varchar(255) primary key," +
                "Holiday DATE);"
			//	+
			//"create table specs (" +
            //    "id varchar(255) primary key," +
            //    "specs TEXT);"
				+
            "create table DelegatedTask (" +
                "CanDelegateTo varchar(255) not null," +
                "TaskID varchar(255) not null," +
                "ResSerPosIDResSerPosID varchar(255) not null, constraint DelegatedTask_PK primary key (CanDelegateTo, ResSerPosIDResSerPosID, TaskID) );"
				+
            "create table CaseStatusChange (" +
                "CaseID varchar(255) not null," +
                "DateTime TIMESTAMP not null," +
                "Casestatus varchar(255) constraint CaseStatusChange_Casestatus null check (Casestatus in ('suspended','resumed','cancelled', 'completed'))," +
                "DoneBy varchar(255), constraint CaseStatusChange_PK primary key (CaseID, DateTime) );"
				+
            "create table HResPosDelegate (" +
                "HResPosID varchar(255) not null," +
                "CanDelegateTo varchar(255) not null, constraint HResPosDelegate_PK primary key (HResPosID, CanDelegateTo) );"
				+
            "create table ResPosSubstitution (" +
                "ResPosID varchar(255) not null," +
                "FromTime TIMESTAMP not null," +
                "ToTime TIMESTAMP not null," +
                "HasSubstitute varchar(255), constraint ResPosSubstitution_PK primary key (ResPosID, FromTime, ToTime) );"
				+
            "create table HResAccessTo (" +
                "HResID varchar(255) not null," +
                "Access BIT," +
				"Tool varchar(255) constraint HResAccessTo_Tool null check (Tool in ('Editor','Administrator Tool','Worklist')), constraint HResAccessTo_PK primary key (HResID) );"
				+
			"create table RoleIncorporatesRole (" +
                "RoleName varchar(255) not null," +
                "IncorporatesRole varchar(255) not null, constraint RoleIncorporatesRole_PK primary key (RoleName, IncorporatesRole) );"
				+
            "create table RolePosition (" +
                "RoleName varchar(255) not null," +
                "PositionID varchar(255) not null, constraint RolePosition_PK primary key (RoleName, PositionID) );"
				+
            "create table HResPerformsRole (" +
                "HResID varchar(255) not null," +
                "RoleName varchar(255) not null, constraint HResPerformsRole_PK primary key (HResID, RoleName) );"
				+
            "create table Role (" +
                "RoleName varchar(255) not null, constraint Role_PK primary key (RoleName) );"
				+
            "create table HResOccupiesPos (" +
                "HResID varchar(255) not null," +
                "PositionID varchar(255) not null, constraint HResOccupiesPos_PK primary key (HResID, PositionID) );"
				+
			"create table TaskSpec (" +
                "SpecID varchar(255) not null," +
                "TaskID varchar(255) not null, constraint TaskSpec_PK primary key (SpecID, TaskID) );"
				+
            "create table Capability (" +
                "Capabilitydesc varchar(255) not null, constraint Capability_PK primary key (Capabilitydesc) );"
				+
            "create table ResourceCapability (" +
                "ResourceID varchar(255) not null," +
                "Capabilitydesc varchar(255), " +
                "constraint ResourceCapability_PK primary key (ResourceID) );"
				+
            "create table Position (" +
                "PositionID varchar(255) not null," +
                "PositionName varchar(255)," +
                "BelongsToOrgGroup varchar(255), constraint Position_PK primary key (PositionID) );"
				+
            /*"create table logdata (" +
                "rowkey varchar(255) not null," +
                "eventid varchar(255)," +
                "port varchar(255)," +
				"value varchar(255)," +
                "io varchar(255), constraint logdata_PK primary key (rowkey) );"
				+
            "create table RUNNER_ENABLED_TASKS (" +
                "runner_id varchar(255) not null," +
                "task varchar(255), constraint RUNNER_ENABLED_TASKS_PK primary key (runner_id) );"
				+
            "create table RUNNER_STATES (" +
               "case_id varchar(255) not null," +
                "identifier varchar(255)," +
                "casedata varchar(255)," +
                "net_id varchar(255)," +
                "containing_task_id varchar(255), constraint RUNNER_STATES_PK primary key (case_id) );"
				+
            "create table YIdentifiers (" +
                "id varchar(255) not null," +
                "parent varchar(255)," +
               "ix integer, constraint YIdentifiers_PK primary key (id) );"
				+
            "create table Work_Items (" +
                "id varchar(255) not null," +
                "specs varchar(255)," +
                "enablement_time TIMESTAMP," +
                "firing_time TIMESTAMP," +
                "start_time TIMESTAMP," +
                "status varchar(255) constraint Work_Items_status null check (status in ('enabled','fired','executing','is_parent','cancelled','complete','deadlocked'))," +
                "parent varchar(255)," +
                "who_started_me varchar(255)," +
                "allows_dynamic_creation BIT," +
                "data_string TEXT," +
                "parent_id varchar(255)," +
                "hasPriority integer constraint Work_Items_hasPriority null check (hasPriority between 0 and 1000), constraint Work_Items_PK primary key (id) );"
				+
            "create table workitemevent (" +
                "rowkey varchar(255) not null," +
                "identifier varchar(255)," +
               "taskid varchar(255)," +
                "resource varchar(255)," +
                "time TIMESTAMP," +
                "event varchar(255)," +
               "description varchar(255), constraint workitemevent_PK primary key (rowkey) );"
				+
            "create table LOCATIONS (" +
                "id_key varchar(255) not null," +
                "ix integer not null," +
                "condition varchar(255), constraint LOCATIONS_PK primary key (id_key, ix) );"
				+
            "create table RUNNER_BUSY_TASKS (" +
                "runner_id varchar(255) not null," +
                "task varchar(255) null, constraint RUNNER_BUSY_TASKS_PK primary key (runner_id) );"
				+
            "create table logidentifiers (" +
                "case_id varchar(255) not null," +
                "cancelled TIMESTAMP," +
				"created TIMESTAMP," +
                "createdby varchar(255)," +
                "specification varchar(255)," +
                "completed TIMESTAMP," +
                "parent varchar(255)," +
                "hasPriority integer constraint logidentifiers_hasPriority null check (hasPriority between 0 and 1000)," +
                "hasDeadline TIMESTAMP, constraint LogIdentifiers_PK primary key (case_id) );"
				+
			"create table CaseDataDocument (" +
                "net_id varchar(255) not null," +
                "casedata TEXT, constraint CaseDataDocument_PK primary key (net_id) );"
				+ */
            "create table Specifications (" +
                "SpecificationID varchar(255) not null," +
                "Description varchar(255)," +
                "CreatedOn TIMESTAMP," +
                "CreatedBy varchar(255)," +
                "VersionNr varchar(255)," +
                "ValidFrom TIMESTAMP," +
                "ValidUntil TIMESTAMP, constraint Specifications_PK primary key (SpecificationID) );"
				+
            "create table OrgGroupWorkingDays (" +
                "OrgGroupName varchar(255) not null," +
                "Sunday BIT," +
                "Monday BIT," +
                "Tuesday BIT," +
                "Wednesday BIT," +
                "Thursday BIT," +
                "Friday BIT," +
                "Saturday BIT, constraint OrgGroupWorkingDays_PK primary key (OrgGroupName) );"
				+
            "create table OrgGroupPartOfOrgGroup (" +
                "OrgGroupName varchar(255) not null," +
                "PartOfOrgGroupName varchar(255) not null, constraint OrgGroupPartOfOrgGroup_PK primary key (OrgGroupName, PartOfOrgGroupName) );"
				+
            "create table OrgGroup (" +
                "OrgGroupName varchar(255) not null," +
                "OrgGroupType varchar(255) constraint OrgGroup_OrgGroupType not null check (OrgGroupType in ('Group','Unit','Team','Branch','Division','Organisation'))," +
                "WorkingDayStartsAt integer constraint OrgGroup_WorkingDayStartsAt null check (WorkingDayStartsAt between 00 and 23)," +
                "WorkingDayFinishesAt integer constraint OrgGroup_WorkingDayFinishesAt null check (WorkingDayFinishesAt between 00 and 23), constraint OrgGroup_PK primary key (OrgGroupName) );"
				+
            "create table ResSerPosID (" +
                "ID varchar(255) not null," +
                "IsOfResSerPosType varchar(255) constraint ResSerPosID_IsOfResSerPosTyp not null check (IsOfResSerPosType in ('Resource','Service','Position'))," +
                "IsOfResourceType varchar(255) constraint ResSerPosID_IsOfResourceType null check (IsOfResourceType in ('Human','Non-Human'))," +
                "ResourceDescription varchar(255)," +
                "UsesWorklist varchar(255)," +
                "SurName varchar(255)," +
                "GivenName varchar(255)," +
                "Password varchar(255)," +
                "IsAdministrator boolean," +
                "ServiceUsername varchar(255)," +
                "ServiceURI varchar(255)," +
                "ServiceDescription varchar(255), " +
                "constraint ResSerPosID_PK primary key (ID) );"
				+
            "create table Tasks (" +
                "id varchar(255) not null," +
                "description varchar(255)," +
                "hasPriority integer constraint Tasks_hasPriority null check (hasPriority between 0 and 1000), constraint Tasks_PK primary key (id) );"
				+
		/*create table Users (" +
				"userid varchar(255) not null," +
				"admin BIT," +
				"password varchar(255), constraint Users_PK primary key (userid) );"
				+
			"create table Services (" +
				"serviceid varchar(255) not null," +
				"documentation varchar(255), constraint Services_PK primary key (serviceid) );"
				+ */
				"create table Worklist (" +
					"worklistname varchar(255) not null," +
					"description varchar(255)," +
					"displaysitems TEXT," +
					"orderBy varchar(255), constraint Worklist_PK primary key (worklistname) );"
					+
				"create table Reports  (" +
					"reportname varchar(255) primary key);"
					+
				"create table Warnings (" +
					"workitemID varchar(255) not null, " +
					"time TIMESTAMP not null, " +
					"MessageType varchar(255), " +
					"XMLMessage TEXT, constraint Warnings_PK primary key (workitemID, time) );"
					+
    			"create unique index Position_AK1 on Position (PositionName);"
    				+
                "alter table Position add constraint Position_AK1_uc1 unique (PositionName);"
    				+
                "create unique index Tasks_AK1 on Tasks (description);"
    				+
                "alter table Tasks add constraint Tasks_AK1_uc2 unique (description);"
    				+
                "alter table Holidays add constraint OrgGroup_Holidays_FK1 foreign key (OrgGroupName) references OrgGroup (OrgGroupName);"
    				+
                "alter table DelegatedTask add constraint Tasks_DelegatedTask_FK1 foreign key (TaskID) references Tasks (id);"
    				+
                "alter table DelegatedTask add constraint HResPosDelegate_DelegatedTask_FK1 foreign key (CanDelegateTo, ResSerPosIDResSerPosID) references HResPosDelegate (HResPosID, CanDelegateTo);"
                    +
                "alter table CaseStatusChange add constraint ResSerPosID_CaseStatusChange_FK1 foreign key (DoneBy) references ResSerPosID (ID);"
    				+
                "alter table HResPosDelegate add constraint ResSerPosID_HResPosDelegate_FK1 foreign key (HResPosID) references ResSerPosID (ID);"
    				+
                "alter table HResPosDelegate add constraint ResSerPosID_HResPosDelegate_FK2 foreign key (CanDelegateTo) references ResSerPosID (ID);"
    				+
                "alter table ResPosSubstitution add constraint ResSerPosID_ResPosSubstitution_FK1 foreign key (ResPosID) references ResSerPosID (ID);"
    				+
                "alter table ResPosSubstitution add constraint ResSerPosID_ResPosSubstitution_FK2 foreign key (HasSubstitute) references ResSerPosID (ID);"
    				+
                "alter table HResAccessTo add constraint ResSerPosID_HResAccessTo_FK1 foreign key (HResID)references ResSerPosID (ID);"
    				+
                "alter table RoleIncorporatesRole add constraint Role_RoleIncorporatesRole_FK1 foreign key (RoleName) references Role (RoleName);"
    				+
                "alter table RoleIncorporatesRole add constraint Role_RoleIncorporatesRole_FK2 foreign key (IncorporatesRole) references Role (RoleName);"
    				+
                "alter table RolePosition add constraint Role_RolePosition_FK1 foreign key (RoleName) references Role (RoleName);"
    				+
                "alter table RolePosition add constraint Position_RolePosition_FK1 foreign key (PositionID) references Position (PositionID);"
    				+
                "alter table HResPerformsRole add constraint ResSerPosID_HResPerformsRole_FK1 foreign key (HResID) references ResSerPosID (ID);"
    				+
                "alter table HResPerformsRole add constraint Role_HResPerformsRole_FK1 foreign key (RoleName) references Role (RoleName);"
    				+
                "alter table HResOccupiesPos add constraint ResSerPosID_HResOccupiesPos_FK1 foreign key (HResID) references ResSerPosID (ID);"
    				+
                "alter table HResOccupiesPos add constraint Position_HResOccupiesPos_FK1 foreign key (PositionID) references Position (PositionID);"
    				+
                "alter table TaskSpec add constraint Specifications_TaskSpec_FK1 foreign key (SpecID) references Specifications (SpecificationID);"
    				+
                "alter table TaskSpec add constraint Tasks_TaskSpec_FK1 foreign key (TaskID) references Tasks (id);"
    				+
                "alter table ResourceCapability add constraint ResSerPosID_ResourceCapability_FK1 foreign key (ResourceID) references ResSerPosID (ID);"
                    +
                "alter table ResourceCapability add constraint Capability_ResourceCapability_FK1 foreign key (Capabilitydesc) references Capability (Capabilitydesc);"
    				+
                "alter table Position add constraint OrgGroup_Position_FK1 foreign key (BelongsToOrgGroup) references OrgGroup (OrgGroupName);"
    				+
                "alter table Position add constraint ResSerPosID_Position_FK1 foreign key (PositionID) references ResSerPosID (ID);"
    				+
                "alter table Specifications add constraint ResSerPosID_Specifications_FK1 foreign key (CreatedBy) references ResSerPosID (ID);"
    				+
                "alter table OrgGroupWorkingDays add constraint OrgGroup_OrgGroupWorkingDays_FK1 foreign key (OrgGroupName) references OrgGroup (OrgGroupName);"
    				+
                "alter table OrgGroupPartOfOrgGroup add constraint OrgGroup_OrgGroupPartOfOrgGroup_FK1 foreign key (OrgGroupName) references OrgGroup (OrgGroupName);"
    				+
                "alter table OrgGroupPartOfOrgGroup add constraint OrgGroup_OrgGroupPartOfOrgGroup_FK2 foreign key (PartOfOrgGroupName) references OrgGroup (OrgGroupName);"
        	);

    }

}
