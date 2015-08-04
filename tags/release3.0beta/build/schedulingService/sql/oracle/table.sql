CREATE TABLE MAPPING
(
  WORKITEMID      VARCHAR2(255 BYTE)            NOT NULL,
  REQUESTKEY      NUMBER(10),
  ISLOCKED        VARCHAR2(1 BYTE)              NOT NULL,
  WORKITEMSTATUS  VARCHAR2(35 BYTE)
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

CREATE TABLE QRTZ_BLOB_TRIGGERS
(
  TRIGGER_NAME   VARCHAR2(200 BYTE)             NOT NULL,
  TRIGGER_GROUP  VARCHAR2(200 BYTE)             NOT NULL,
  BLOB_DATA      BLOB
)
LOB (BLOB_DATA) STORE AS (
  TABLESPACE SCHEDULING
  ENABLE       STORAGE IN ROW
  CHUNK       8192
  RETENTION
  NOCACHE
  LOGGING
  INDEX       (
        TABLESPACE SCHEDULING
        STORAGE    (
                    INITIAL          64K
                    NEXT             1M
                    MINEXTENTS       1
                    MAXEXTENTS       UNLIMITED
                    PCTINCREASE      0
                    BUFFER_POOL      DEFAULT
                   ))
      STORAGE    (
                  INITIAL          64K
                  NEXT             1M
                  MINEXTENTS       1
                  MAXEXTENTS       UNLIMITED
                  PCTINCREASE      0
                  BUFFER_POOL      DEFAULT
                 ))
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;


CREATE TABLE QRTZ_CALENDARS
(
  CALENDAR_NAME  VARCHAR2(200 BYTE)             NOT NULL,
  CALENDAR       BLOB                           NOT NULL
)
LOB (CALENDAR) STORE AS (
  TABLESPACE SCHEDULING
  ENABLE       STORAGE IN ROW
  CHUNK       8192
  RETENTION
  NOCACHE
  LOGGING
  INDEX       (
        TABLESPACE SCHEDULING
        STORAGE    (
                    INITIAL          64K
                    NEXT             1M
                    MINEXTENTS       1
                    MAXEXTENTS       UNLIMITED
                    PCTINCREASE      0
                    BUFFER_POOL      DEFAULT
                   ))
      STORAGE    (
                  INITIAL          64K
                  NEXT             1M
                  MINEXTENTS       1
                  MAXEXTENTS       UNLIMITED
                  PCTINCREASE      0
                  BUFFER_POOL      DEFAULT
                 ))
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

               
CREATE TABLE QRTZ_CRON_TRIGGERS
(
  TRIGGER_NAME     VARCHAR2(200 BYTE)           NOT NULL,
  TRIGGER_GROUP    VARCHAR2(200 BYTE)           NOT NULL,
  CRON_EXPRESSION  VARCHAR2(120 BYTE)           NOT NULL,
  TIME_ZONE_ID     VARCHAR2(80 BYTE)
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;


CREATE TABLE QRTZ_FIRED_TRIGGERS
(
  ENTRY_ID           VARCHAR2(95 BYTE)          NOT NULL,
  TRIGGER_NAME       VARCHAR2(200 BYTE)         NOT NULL,
  TRIGGER_GROUP      VARCHAR2(200 BYTE)         NOT NULL,
  IS_VOLATILE        VARCHAR2(1 BYTE)           NOT NULL,
  INSTANCE_NAME      VARCHAR2(200 BYTE)         NOT NULL,
  FIRED_TIME         NUMBER(13)                 NOT NULL,
  PRIORITY           NUMBER(13)                 NOT NULL,
  STATE              VARCHAR2(16 BYTE)          NOT NULL,
  JOB_NAME           VARCHAR2(200 BYTE),
  JOB_GROUP          VARCHAR2(200 BYTE),
  IS_STATEFUL        VARCHAR2(1 BYTE),
  REQUESTS_RECOVERY  VARCHAR2(1 BYTE)
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;


CREATE INDEX IDX_QRTZ_FT_JOB_GROUP ON QRTZ_FIRED_TRIGGERS
(JOB_GROUP)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_FT_JOB_NAME ON QRTZ_FIRED_TRIGGERS
(JOB_NAME)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_FT_JOB_REQ_RECOVERY ON QRTZ_FIRED_TRIGGERS
(REQUESTS_RECOVERY)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_FT_JOB_STATEFUL ON QRTZ_FIRED_TRIGGERS
(IS_STATEFUL)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_FT_TRIG_GROUP ON QRTZ_FIRED_TRIGGERS
(TRIGGER_GROUP)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS
(INSTANCE_NAME)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_FT_TRIG_NAME ON QRTZ_FIRED_TRIGGERS
(TRIGGER_NAME)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_FT_TRIG_NM_GP ON QRTZ_FIRED_TRIGGERS
(TRIGGER_NAME, TRIGGER_GROUP)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_FT_TRIG_VOLATILE ON QRTZ_FIRED_TRIGGERS
(IS_VOLATILE)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;

               
CREATE TABLE QRTZ_JOB_DETAILS
(
  JOB_NAME           VARCHAR2(200 BYTE)         NOT NULL,
  JOB_GROUP          VARCHAR2(200 BYTE)         NOT NULL,
  DESCRIPTION        VARCHAR2(250 BYTE),
  JOB_CLASS_NAME     VARCHAR2(250 BYTE)         NOT NULL,
  IS_DURABLE         VARCHAR2(1 BYTE)           NOT NULL,
  IS_VOLATILE        VARCHAR2(1 BYTE)           NOT NULL,
  IS_STATEFUL        VARCHAR2(1 BYTE)           NOT NULL,
  REQUESTS_RECOVERY  VARCHAR2(1 BYTE)           NOT NULL,
  JOB_DATA           BLOB
)
LOB (JOB_DATA) STORE AS (
  TABLESPACE SCHEDULING
  ENABLE       STORAGE IN ROW
  CHUNK       8192
  RETENTION
  NOCACHE
  LOGGING
  INDEX       (
        TABLESPACE SCHEDULING
        STORAGE    (
                    INITIAL          64K
                    NEXT             1M
                    MINEXTENTS       1
                    MAXEXTENTS       UNLIMITED
                    PCTINCREASE      0
                    BUFFER_POOL      DEFAULT
                   ))
      STORAGE    (
                  INITIAL          64K
                  NEXT             1M
                  MINEXTENTS       1
                  MAXEXTENTS       UNLIMITED
                  PCTINCREASE      0
                  BUFFER_POOL      DEFAULT
                 ))
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;


CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS
(REQUESTS_RECOVERY)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;

               
CREATE TABLE QRTZ_JOB_LISTENERS
(
  JOB_NAME      VARCHAR2(200 BYTE)              NOT NULL,
  JOB_GROUP     VARCHAR2(200 BYTE)              NOT NULL,
  JOB_LISTENER  VARCHAR2(200 BYTE)              NOT NULL
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

  
CREATE TABLE QRTZ_LOCKS
(
  LOCK_NAME  VARCHAR2(40 BYTE)                  NOT NULL
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

               
CREATE TABLE QRTZ_PAUSED_TRIGGER_GRPS
(
  TRIGGER_GROUP  VARCHAR2(200 BYTE)             NOT NULL
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

               
CREATE TABLE QRTZ_SCHEDULER_STATE
(
  INSTANCE_NAME      VARCHAR2(200 BYTE)         NOT NULL,
  LAST_CHECKIN_TIME  NUMBER(13)                 NOT NULL,
  CHECKIN_INTERVAL   NUMBER(13)                 NOT NULL
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;

               
CREATE TABLE QRTZ_SIMPLE_TRIGGERS
(
  TRIGGER_NAME     VARCHAR2(200 BYTE)           NOT NULL,
  TRIGGER_GROUP    VARCHAR2(200 BYTE)           NOT NULL,
  REPEAT_COUNT     NUMBER(7)                    NOT NULL,
  REPEAT_INTERVAL  NUMBER(12)                   NOT NULL,
  TIMES_TRIGGERED  NUMBER(10)                   NOT NULL
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;


CREATE TABLE QRTZ_TRIGGERS
(
  TRIGGER_NAME    VARCHAR2(200 BYTE)            NOT NULL,
  TRIGGER_GROUP   VARCHAR2(200 BYTE)            NOT NULL,
  JOB_NAME        VARCHAR2(200 BYTE)            NOT NULL,
  JOB_GROUP       VARCHAR2(200 BYTE)            NOT NULL,
  IS_VOLATILE     VARCHAR2(1 BYTE)              NOT NULL,
  DESCRIPTION     VARCHAR2(250 BYTE),
  NEXT_FIRE_TIME  NUMBER(13),
  PREV_FIRE_TIME  NUMBER(13),
  PRIORITY        NUMBER(13),
  TRIGGER_STATE   VARCHAR2(16 BYTE)             NOT NULL,
  TRIGGER_TYPE    VARCHAR2(8 BYTE)              NOT NULL,
  START_TIME      NUMBER(13)                    NOT NULL,
  END_TIME        NUMBER(13),
  CALENDAR_NAME   VARCHAR2(200 BYTE),
  MISFIRE_INSTR   NUMBER(2),
  JOB_DATA        BLOB
)
LOB (JOB_DATA) STORE AS (
  TABLESPACE SCHEDULING
  ENABLE       STORAGE IN ROW
  CHUNK       8192
  RETENTION
  NOCACHE
  LOGGING
  INDEX       (
        TABLESPACE SCHEDULING
        STORAGE    (
                    INITIAL          64K
                    NEXT             1M
                    MINEXTENTS       1
                    MAXEXTENTS       UNLIMITED
                    PCTINCREASE      0
                    BUFFER_POOL      DEFAULT
                   ))
      STORAGE    (
                  INITIAL          64K
                  NEXT             1M
                  MINEXTENTS       1
                  MAXEXTENTS       UNLIMITED
                  PCTINCREASE      0
                  BUFFER_POOL      DEFAULT
                 ))
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;


CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS
(NEXT_FIRE_TIME)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS
(NEXT_FIRE_TIME, TRIGGER_STATE)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS
(TRIGGER_STATE)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;


CREATE INDEX IDX_QRTZ_T_VOLATILE ON QRTZ_TRIGGERS
(IS_VOLATILE)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;

CREATE TABLE QRTZ_TRIGGER_LISTENERS
(
  TRIGGER_NAME      VARCHAR2(200 BYTE)          NOT NULL,
  TRIGGER_GROUP     VARCHAR2(200 BYTE)          NOT NULL,
  TRIGGER_LISTENER  VARCHAR2(200 BYTE)          NOT NULL
)
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;


CREATE TABLE UTILISATIONPLAN
(
  CASE_ID        NUMBER(10)                     NOT NULL,
  PLAN_          SYS.XMLTYPE,
  NAME_          VARCHAR2(50 BYTE),
  DESCRIPTION    VARCHAR2(1000 BYTE),
  LASTEDITED_BY  VARCHAR2(30 BYTE),
  LASTEDITED     TIMESTAMP(3),
  ACTIVE         NUMBER(1)                      DEFAULT 1
)
XMLTYPE PLAN_ STORE AS CLOB (
  TABLESPACE SCHEDULING
  ENABLE       STORAGE IN ROW
  CHUNK       8192
  RETENTION
  NOCACHE
  LOGGING
  INDEX       (
        TABLESPACE SCHEDULING
        STORAGE    (
                    INITIAL          64K
                    NEXT             1M
                    MINEXTENTS       1
                    MAXEXTENTS       UNLIMITED
                    PCTINCREASE      0
                    BUFFER_POOL      DEFAULT
                   ))
      STORAGE    (
                  INITIAL          64K
                  NEXT             1M
                  MINEXTENTS       1
                  MAXEXTENTS       UNLIMITED
                  PCTINCREASE      0
                  BUFFER_POOL      DEFAULT
                 ))
TABLESPACE SCHEDULING
PCTUSED    0
PCTFREE    10
INITRANS   1
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
LOGGING 
NOCOMPRESS 
NOCACHE
NOPARALLEL
MONITORING;


CREATE UNIQUE INDEX UTILISATIONPLAN_PK ON UTILISATIONPLAN
(CASE_ID)
LOGGING
TABLESPACE SCHEDULING
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            INITIAL          64K
            NEXT             1M
            MINEXTENTS       1
            MAXEXTENTS       UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           )
NOPARALLEL;

