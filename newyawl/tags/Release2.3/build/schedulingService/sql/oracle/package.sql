CREATE OR REPLACE PACKAGE            PKG_UTILISATIONPLAN AS
/******************************************************************************
   NAME:       PKG_UTILISATIONPLAN
   PURPOSE:

   REVISIONS:
   Ver        Date        Author           Description
   ---------  ----------  ---------------  ------------------------------------
   1.0        20.12.2010      jku       1. Created this package.
******************************************************************************/

  FUNCTION savePlan(p_CaseId number, p_UtilisationPlanParts IN stringarray, p_Name varchar2, p_Description varchar2, p_LastEditedBy varchar2, p_LastEdited timestamp) RETURN NUMBER;


END PKG_UTILISATIONPLAN;
/


CREATE OR REPLACE PACKAGE BODY            PKG_UTILISATIONPLAN
as
	FUNCTION saveplan (p_CaseId NUMBER, p_UtilisationPlanParts stringarray,  p_Name VARCHAR2, p_Description VARCHAR2, p_LastEditedBy VARCHAR2, p_LastEdited TIMESTAMP)
	RETURN  NUMBER
	IS
		v_DmlCount		 NUMBER(10)	:=1;
      v_Count				  NUMBER(10);
      v_Plan					XMLTYPE;
   	v_Clob  					CLOB;
      v_Part					VARCHAR2(1000);
	BEGIN
   	FOR i in p_UtilisationPlanParts.FIRST .. p_UtilisationPlanParts.LAST LOOP
   			v_Clob:=v_Clob || p_UtilisationPlanParts(i);
   	END LOOP;
      v_Plan:=XMLTYPE.createXML(v_Clob); 

		SELECT 	 count(1)
      INTO			 v_Count
		FROM			UTILISATIONPLAN
      WHERE		 case_id=p_CaseId;
      
      IF (v_Count = 0) THEN
			INSERT
         INTO		     UTILISATIONPLAN (case_id, plan_,name_, description, lastedited_by, lastedited)
         VALUES 		(p_CaseId, v_Plan,p_Name,p_Description,p_LastEditedBy, p_LastEdited);
         v_DmlCount:=SQL%ROWCOUNT;
      ELSE
      	UPDATE	UTILISATIONPLAN
         SET		plan_ = v_Plan,
         				 name_= p_Name, 
                      description= p_Description, 
                      lastedited_by=p_LastEditedBy, 
                      lastedited=p_LastEdited
         WHERE		case_id=p_CaseID;
         v_DmlCount:=SQL%ROWCOUNT;
      END IF;      
      COMMIT;         
		RETURN v_DmlCount;
	END saveplan;
   
end PKG_UTILISATIONPLAN;
/
CREATE OR REPLACE PACKAGE            PKG_MAPPING 
AS

   TYPE t_Cursor IS REF CURSOR;


-------------------------------------------------------------------------------
-- Procedure:  SAVE_(...)
-- Purpose:    Saves a mapping relationship (workitemid,requestkey) to persistent storage.
-- Created:    6.4.2009   Jan-Christian Kuhr
-- Changed:    20.01.2010 Marcel Paleit
--             Exception handling: raises now an application error message
--             04.02.2010 Marcel Paleit
--             Now, a mapping is either inserted or updated depending on whether the workitem ID is already stored 
-- Steps:      
-- Parameters:
--          p_WorkitemId            :   unique ID of a YAWL work item, defined by YAWL.
--          p_RequestKey             :   unique ID of a FilterInstace which maps the latter to a WorkItem of YAWL
--          p_WorkItemStatus    :   state of the workitem (e.g. cached, ...), more precise descriptions holds the java code ...
-- Throws:  
-------------------------------------------------------------------------------
   PROCEDURE SAVE_
   (p_WorkitemId MAPPING.workitemid%TYPE,
      p_RequestKey MAPPING.requestkey%TYPE,
      p_WorkItemStatus MAPPING.workitemstatus%TYPE);
      
------------------------------------------------------------------------------
-- Procedure:  REMOVE_(...)
-- Purpose:    Removes a mapping relationship (workitemid,requestkey) from persistent storage whether it is locked or not.
-- Created:    11.02.2010   Marcel Paleit
-- Changed:    
-- Steps:      
-- Parameters:
--             p_WorkitemId     :   unique ID of a YAWL work item
--             p_RequestKey     :    unique key of a JCoupling request (JCoupling assigns a request key to every individual filter instance)
-- Throws:
------------------------------------------------------------------------------- 
   PROCEDURE REMOVE_(p_WorkItemID MAPPING.workitemid%TYPE , p_RequestKey MAPPING.requestkey%TYPE );

-------------------------------------------------------------------------------
-- Procedure:  UNLOCK_(...)
-- Purpose:    Unlocks the mapping that is specified by the workitem ID
-- Created:    28.4.2009   Jan-Christian Kuhr
-- Changed:    20.01.2010 Marcel Paleit - Exception handling raises now an application error message
-- Steps:      
-- Parameters:
--             p_WorkitemId     :   unique ID of a YAWL work item
-- Throws:     
-------------------------------------------------------------------------------    
   PROCEDURE UNLOCK_(p_WorkItemID MAPPING.workitemid%TYPE);


 -------------------------------------------------------------------------------
-- Procedure:  GET_MAPPINGS(...)
-- Purpose:    Locks and retrieves all mappings for a given channel
-- Created:    08.02.2010   Marcel Paleit
-- Changed:  6.4.2010 Jan-Christian  (changed function into procedure, combined SELECTand UPDATE in a single method, set isolation level to SERIALIZABLE)
--                  9.4.2010 Jan-Christian / Thomas (changed WHERE clause in SELECT and UPDATE statements to allow for dangling tuples by means of left outer joins) 
-- Parameters: 
-- Returns:    Table of type T_MAPPINGTAB 
-- Throws:     
-------------------------------------------------------------------------------  
  PROCEDURE GET_MAPPINGS(p_MappingTab OUT t_MappingTab);
   
END PKG_MAPPING;
/


CREATE OR REPLACE PACKAGE BODY            PKG_MAPPING
AS
   PROCEDURE get_mappings (p_MappingTab    OUT t_MappingTab)
    AS
        i                        		NUMBER (9);
        v_MappingRow        MAPPING%ROWTYPE;
        v_MappingRecord    MAPPINGROW		:= MAPPINGROW (NULL,NULL,NULL,NULL);
        v_MappingTab         T_MAPPINGTAB := T_MAPPINGTAB ();

        CURSOR c_MappingCursor IS
            SELECT      m.*
            FROM          MAPPING m
            WHERE       m.islocked = 'N'
            FOR UPDATE ;
    BEGIN
        OPEN c_MappingCursor;
        i := 1;
        LOOP
            FETCH c_MappingCursor INTO v_MappingRow;
            EXIT WHEN c_MappingCursor%NOTFOUND;            
            v_MappingTab.EXTEND (1);
            v_MappingRecord.workitemid := v_MappingRow.workitemid;
            v_MappingRecord.requestKey := v_MappingRow.requestkey;
            v_MappingRecord.islocked := v_MappingRow.islocked;
            v_MappingRecord.status := v_MappingRow.workitemstatus;
            v_MappingTab(i) := v_MappingRecord;
            i := i + 1;
        END LOOP;

        UPDATE MAPPING
        SET     islocked = 'Y'
        WHERE  workitemid IN
                     (SELECT m.workitemid
                      FROM    MAPPING m
                      WHERE    m.islocked = 'N');
        CLOSE c_MappingCursor;
        COMMIT;
        p_MappingTab := v_MappingTab;
    END get_mappings;
   
  PROCEDURE save_ (
                          p_WorkitemId MAPPING.workitemid%TYPE,
                          p_RequestKey MAPPING.requestkey%TYPE,
                          p_WorkitemStatus MAPPING.workitemstatus%TYPE
                         )
    AS
        CURSOR c_Mapping
        IS
            SELECT      *
            FROM        MAPPING
            WHERE       workitemid = p_WorkitemId
            FOR UPDATE ;
    BEGIN
        OPEN c_Mapping;
            UPDATE MAPPING
            SET     requestkey = p_RequestKey, workitemstatus = p_WorkitemStatus
            WHERE  workitemid = p_WorkitemId;

            IF (SQL%ROWCOUNT = 0) THEN
                BEGIN
                    INSERT INTO  MAPPING (
                                                 workitemid,
                                                 requestkey,
                                                 islocked,
                                                 workitemstatus
                                                )
                    VALUES         (
                                      p_WorkitemId,
                                      p_RequestKey,
                                      'Y',
                                      p_WorkitemStatus
                                     );
                EXCEPTION
                    WHEN DUP_VAL_ON_INDEX THEN
                        DBMS_OUTPUT.put_line (SQLERRM);
                END;
            END IF;
            COMMIT;
        CLOSE c_Mapping;
    END save_;
 
    PROCEDURE remove_ (
                             p_WorkitemId MAPPING.workitemid%TYPE,
                             p_RequestKey MAPPING.requestkey%TYPE
                            )
    AS
    BEGIN     
        IF (p_RequestKey IS NULL) THEN
            DELETE 
            FROM 			MAPPING
            WHERE     workitemid like p_WorkitemId ||'%';
        ELSE
            DELETE 
            FROM 			  MAPPING
            WHERE        workitemid = p_WorkitemId AND 
            							requestkey = p_RequestKey;
        END IF;
        COMMIT;
    END remove_;
  
    PROCEDURE unlock_ (p_WorkitemId MAPPING.workitemid%TYPE)
    AS
        v_WorkitemId MAPPING.workitemid%TYPE;
        v_Count             number(10);
   BEGIN 
              
        UPDATE MAPPING
        SET     islocked = 'N'
        WHERE  workitemid = p_WorkitemId;
        
        COMMIT;
        
    END unlock_;
END PKG_MAPPING;
/
