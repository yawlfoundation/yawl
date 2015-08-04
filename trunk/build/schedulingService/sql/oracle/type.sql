CREATE OR REPLACE TYPE              "MAPPINGROW"                                          AS OBJECT 
( WorkItemId varchar2(255),
  RequestKey number,
  ISLOCKED  varchar2(1),
  status   varchar2(200)
);
/
CREATE OR REPLACE TYPE              "STRINGARRAY"                                          AS TABLE OF varchar2(3000);
/
CREATE OR REPLACE TYPE              "T_MAPPINGTAB"                                           AS TABLE OF MAPPINGROW;
/
