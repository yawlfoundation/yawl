--
-- PostgreSQL database dump
--

-- Started on 2011-09-02 18:23:06

SET statement_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = off;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET escape_string_warning = off;

--
-- TOC entry 7 (class 2615 OID 16850)
-- Name: pkg_mapping; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA pkg_mapping;


ALTER SCHEMA pkg_mapping OWNER TO postgres;

--
-- TOC entry 9 (class 2615 OID 17168)
-- Name: pkg_util; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA pkg_util;


ALTER SCHEMA pkg_util OWNER TO postgres;

--
-- TOC entry 1851 (class 0 OID 0)
-- Dependencies: 9
-- Name: SCHEMA pkg_util; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON SCHEMA pkg_util IS 'Package (schema) that contains utility functions';


--
-- TOC entry 6 (class 2615 OID 16849)
-- Name: pkg_utilisationplan; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA pkg_utilisationplan;


ALTER SCHEMA pkg_utilisationplan OWNER TO postgres;

--
-- TOC entry 348 (class 2612 OID 16845)
-- Name: plpgsql; Type: PROCEDURAL LANGUAGE; Schema: -; Owner: postgres
--

-- CREATE PROCEDURAL LANGUAGE plpgsql;
CREATE TRUSTED PROCEDURAL LANGUAGE "plpgsql"
  HANDLER plpgsql_call_handler
  VALIDATOR plpgsql_validator;

ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO postgres;

SET search_path = public, pg_catalog;

--
-- TOC entry 272 (class 1247 OID 16848)
-- Dependencies: 3 1520
-- Name: mappingrow; Type: TYPE; Schema: public; Owner: postgres
--

CREATE TYPE mappingrow AS (
	"WorkItemId" character varying(255),
	"RequestKey" integer,
	"isLocked" character varying(1),
	status character varying(200)
);


ALTER TYPE public.mappingrow OWNER TO postgres;

SET search_path = pkg_mapping, pg_catalog;

--
-- TOC entry 36 (class 1255 OID 17153)
-- Dependencies: 7 348
-- Name: get_(); Type: FUNCTION; Schema: pkg_mapping; Owner: postgres
--

CREATE FUNCTION get_() RETURNS refcursor
    LANGUAGE plpgsql
    AS $$
DECLARE
   c_MappingCursor CURSOR IS
      SELECT      m.*
      FROM        mapping m
      WHERE       m.islocked = 'N'
      FOR UPDATE;
BEGIN
   OPEN c_MappingCursor;
   return c_MappingCursor;

EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;      
END;
$$;


ALTER FUNCTION pkg_mapping.get_() OWNER TO postgres;

--
-- TOC entry 23 (class 1255 OID 17151)
-- Dependencies: 7 348
-- Name: lock_(character varying[]); Type: FUNCTION; Schema: pkg_mapping; Owner: postgres
--

CREATE FUNCTION lock_(workitemids character varying[]) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_WorkItemIds  alias for $1;
   v_RowCount     integer  :=0;
   v_Statement    text     := 'UPDATE mapping SET islocked = ' || quote_literal('Y');
   v_ArgList      text;
   i              integer  :=1;
BEGIN
   IF (p_WorkItemIds is null) THEN
      RETURN v_RowCount;
   END IF; 

   v_ArgList := '(';
   WHILE true LOOP
      IF (p_WorkItemIds[i] is NULL) THEN 
         EXIT;
      END IF;
 		v_ArgList := v_Arglist || quote_literal(p_WorkItemIds[i])|| ',';      
 		i := i +1;
   END LOOP;
   v_ArgList := rtrim(v_ArgList,',');
   v_ArgList := v_ArgList || ')';
   
   v_Statement := v_Statement || 'where workitemid in ' || v_ArgList;     
   EXECUTE v_Statement;
   GET DIAGNOSTICS v_RowCount := ROW_COUNT;
 
   return v_RowCount;
EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;
end
$_$;


ALTER FUNCTION pkg_mapping.lock_(workitemids character varying[]) OWNER TO postgres;

--
-- TOC entry 24 (class 1255 OID 17152)
-- Dependencies: 348 7
-- Name: remove_(character varying, integer); Type: FUNCTION; Schema: pkg_mapping; Owner: postgres
--

CREATE FUNCTION remove_("WorkItemId" character varying, "RequestKey" integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_WorkItemId   alias for $1;
   p_RequestKey   alias for $2;
   p_RowCount     integer;
begin
    BEGIN     
        IF (p_RequestKey IS NULL) THEN
            DELETE 
            FROM 		MAPPING
            WHERE    workitemid like p_WorkitemId ||'%';
        ELSE
            DELETE 
            FROM 		MAPPING
            WHERE    workitemid = p_WorkitemId AND 
 							requestkey = p_RequestKey;
        END IF;
        GET DIAGNOSTICS p_RowCount = ROW_COUNT;
   END;
   return p_RowCount;
end;
$_$;


ALTER FUNCTION pkg_mapping.remove_("WorkItemId" character varying, "RequestKey" integer) OWNER TO postgres;

--
-- TOC entry 25 (class 1255 OID 17154)
-- Dependencies: 348 7
-- Name: save_(character varying, integer, character varying); Type: FUNCTION; Schema: pkg_mapping; Owner: postgres
--

CREATE FUNCTION save_("WorkitemId" character varying, "RequestKey" integer, "WorkitemStatus" character varying) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_RowCount        integer  :=0;

   p_WorkitemId      alias for $1;
   p_RequestKey      alias for $2;
   p_WorkitemStatus  alias for $3;
   
   c_Mapping CURSOR FOR
            SELECT      *
            FROM        MAPPING
            WHERE       workitemid = p_WorkitemId 
            FOR UPDATE;   
BEGIN
   OPEN c_Mapping;
      UPDATE   MAPPING
      SET      requestkey = p_RequestKey, 
               workitemstatus = p_WorkitemStatus
      WHERE    workitemid = p_WorkitemId;
      GET DIAGNOSTICS p_RowCount = ROW_COUNT;

      IF (p_RowCount = 0) THEN
         BEGIN
            INSERT 
            INTO  MAPPING (
                           workitemid,
                           requestkey,
                           islocked,
                           workitemstatus)                                  
            VALUES(
                           p_WorkitemId,
                           p_RequestKey,
                           'Y',
                           p_WorkitemStatus);
            p_RowCount = 1;
         EXCEPTION
            WHEN unique_violation THEN
               raise notice '% %', SQLSTATE, SQLERRM;
         END;
      END IF;
   CLOSE c_Mapping;
   return p_RowCount;
END;
$_$;


ALTER FUNCTION pkg_mapping.save_("WorkitemId" character varying, "RequestKey" integer, "WorkitemStatus" character varying) OWNER TO postgres;

--
-- TOC entry 26 (class 1255 OID 17156)
-- Dependencies: 348 7
-- Name: unlock_(character varying[]); Type: FUNCTION; Schema: pkg_mapping; Owner: postgres
--

CREATE FUNCTION unlock_("WorkItemIds" character varying[]) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_WorkItemIds  alias for $1;
   v_RowCount     integer  :=0;
   v_Statement    text     := 'UPDATE mapping SET islocked = ' || quote_literal('N');
   v_ArgList      text;
   i              integer  :=1;
BEGIN
   v_ArgList := '(';
   WHILE true LOOP
      IF (p_WorkItemIds[i] is NULL) THEN 
         EXIT;
      END IF;
 		v_ArgList := v_Arglist || quote_literal(p_WorkItemIds[i])|| ',';      
 		i := i +1;
   END LOOP;
   v_ArgList := rtrim(v_ArgList,',');
   v_ArgList := v_ArgList || ')';
   
   v_Statement := v_Statement || 'where workitemid in ' || v_ArgList;     
   EXECUTE v_Statement;
   GET DIAGNOSTICS v_RowCount := ROW_COUNT;
 
   return v_RowCount;
EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;
END;
$_$;


ALTER FUNCTION pkg_mapping.unlock_("WorkItemIds" character varying[]) OWNER TO postgres;

SET search_path = pkg_util, pg_catalog;

--
-- TOC entry 34 (class 1255 OID 17169)
-- Dependencies: 348 9
-- Name: to_csv_list(character varying[]); Type: FUNCTION; Schema: pkg_util; Owner: postgres
--

CREATE FUNCTION to_csv_list("Elements" character varying[]) RETURNS character varying
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_Elements     alias for $1;
   v_StringList   text;
   i              integer;
BEGIN
   v_StringList := ' ';
   i := 1;
   WHILE true LOOP
      IF (p_Elements[i] is NULL) THEN 
         EXIT;
      END IF;
 		v_StringList := v_StringList || quote_literal(p_Elements[i])|| ',';      
 		i := i +1;
   END LOOP;
   v_StringList := rtrim(v_StringList,',');
   return v_StringList;
EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;
END;
$_$;


ALTER FUNCTION pkg_util.to_csv_list("Elements" character varying[]) OWNER TO postgres;

SET search_path = pkg_utilisationplan, pg_catalog;

--
-- TOC entry 27 (class 1255 OID 17157)
-- Dependencies: 348 6
-- Name: get_active(character varying); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION get_active(timestamp_ character varying) RETURNS refcursor
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_Timestamp                alias for $1;
--   
   c_UtilisationPlan CURSOR IS      
      SELECT   a.case_id as case_id,
               a.plan_ as rupxml,
               a.name_ as spec_name,
               a.description as spec_description,
               a.lastedited_by as lastedited_by,
               a.lastedited as lastedited_ts,
               a.active as active_flag
      FROM     utilisationplan a
      WHERE    (xpath('//Activity/To/text()', a.plan_))[1]::text < p_Timestamp and
               (xpath('//Activity/RequestType/text()', a.plan_))[1]::text = 'SOU' and
               active = 1; 
BEGIN
   OPEN c_UtilisationPlan;
   return c_UtilisationPlan;

EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;      
END;
$_$;


ALTER FUNCTION pkg_utilisationplan.get_active(timestamp_ character varying) OWNER TO postgres;

--
-- TOC entry 31 (class 1255 OID 17159)
-- Dependencies: 348 6
-- Name: get_activity_types(character varying); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION get_activity_types(activity_name character varying) RETURNS refcursor
    LANGUAGE plpgsql
    AS $_$
DECLARE
      p_ActivityName          alias for $1;
      
   c_UtilisationPlan CURSOR IS      
      SELECT   a.case_id as case_id,
               (xpath('//Activity/ActivityType/text()', a.plan_))[1]::text as activity_type
      FROM     utilisationplan a
      WHERE    (xpath('//Activity/ActivityName/text()', a.plan_))[1]::text = p_ActivityName;
                        
BEGIN
  OPEN c_UtilisationPlan;
  RETURN c_UtilisationPlan;

EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;    
END
$_$;


ALTER FUNCTION pkg_utilisationplan.get_activity_types(activity_name character varying) OWNER TO postgres;

--
-- TOC entry 28 (class 1255 OID 17161)
-- Dependencies: 6 348
-- Name: get_all(); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION get_all() RETURNS refcursor
    LANGUAGE plpgsql
    AS $$
DECLARE
--   
   c_UtilisationPlan CURSOR IS      
      SELECT   a.case_id as case_id,
               a.plan_ as rupxml,
               a.name_ as spec_name,
               a.description as spec_description,
               a.lastedited_by as lastedited_by,
               a.lastedited as lastedited_ts,
               a.active as active_flag
      FROM     utilisationplan a;
BEGIN
   OPEN c_UtilisationPlan;
   return c_UtilisationPlan;

EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;      
END;
$$;


ALTER FUNCTION pkg_utilisationplan.get_all() OWNER TO postgres;

--
-- TOC entry 29 (class 1255 OID 17162)
-- Dependencies: 348 6
-- Name: get_by_activity(character varying); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION get_by_activity(activity character varying) RETURNS refcursor
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_Activity               alias for $1;
--   
   c_UtilisationPlan CURSOR IS      
      SELECT   a.case_id as case_id,
               a.plan_ as rupxml,
               a.name_ as spec_name,
               a.description as spec_description,
               a.lastedited_by as lastedited_by,
               a.lastedited as lastedited_ts,
               a.active as active_flag
      FROM     utilisationplan a
      WHERE    (xpath('//Activity/ActivityName/text()', a.plan_))[1]::text = p_Activity; 
BEGIN
   OPEN c_UtilisationPlan;
   return c_UtilisationPlan;

EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;      
END;
$_$;


ALTER FUNCTION pkg_utilisationplan.get_by_activity(activity character varying) OWNER TO postgres;

--
-- TOC entry 30 (class 1255 OID 17163)
-- Dependencies: 6 348
-- Name: get_by_caseid(character varying); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION get_by_caseid(caseid character varying) RETURNS refcursor
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_CaseId                alias for $1;
--   
   c_UtilisationPlan CURSOR IS
      SELECT   a.case_id as case_id,
               a.plan_ as rupxml,
               a.name_ as spec_name,
               a.description as spec_description,
               a.lastedited_by as lastedited_by,
               a.lastedited as lastedited_ts,
               a.active as active_flag
      FROM     utilisationplan a
      WHERE    a.case_id = p_CaseId;
--      FOR UPDATE;
BEGIN
   OPEN c_UtilisationPlan;
   return c_UtilisationPlan;

EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;      
END;
$_$;


ALTER FUNCTION pkg_utilisationplan.get_by_caseid(caseid character varying) OWNER TO postgres;

--
-- TOC entry 35 (class 1255 OID 17164)
-- Dependencies: 348 6
-- Name: get_by_interval(character varying, character varying, character varying[], boolean); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION get_by_interval(time_min character varying, time_max character varying, excluded_caseids character varying[], active boolean) RETURNS refcursor
    LANGUAGE plpgsql
    AS $_$
DECLARE
   p_MinTimestamp       alias for $1;
   p_MaxTimestamp       alias for $2;
   p_ExcludedCaseIds    alias for $3;
   p_ActiveFlag         alias for $4;
--   
   c_ActiveUtilisationPlans CURSOR IS      
       SELECT  a.case_id as case_id,
               a.plan_ as rupxml,
               a.name_ as spec_name,
               a.description as spec_description,
               a.lastedited_by as lastedited_by,
               a.lastedited as lastedited_ts,
               a.active as active_flag
      FROM     utilisationplan a
      WHERE    (xpath('//Activity/From/text()', a.plan_))[1]::text < p_MinTimestamp and
               (xpath('//Activity/To/text()', a.plan_))[1]::text > p_MaxTimestamp and
               a.case_id not in (pkg_util.to_csv_list(p_ExcludedCaseIds)) and
               a.active = 1;

   c_UtilisationPlans CURSOR IS      
      SELECT   a.case_id as case_id,
               a.plan_ as rupxml,
               a.name_ as spec_name,
               a.description as spec_description,
               a.lastedited_by as lastedited_by,
               a.lastedited as lastedited_ts,
               a.active as active_flag
      FROM     utilisationplan a
      WHERE    (xpath('//Activity/From/text()', a.plan_))[1]::text < p_MinTimestamp and
               (xpath('//Activity/To/text()', a.plan_))[1]::text > p_MaxTimestamp and
               a.case_id not in (pkg_util.to_csv_list(p_ExcludedCaseIds));
               

BEGIN
   IF (p_ActiveFlag is true) THEN
      OPEN c_ActiveUtilisationPlans;
      RETURN c_ActiveUtilisationPlans;
   ELSE
      OPEN c_UtilisationPlans;
      RETURN c_UtilisationPlans;   
   END IF;   

EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;     
END;
$_$;


ALTER FUNCTION pkg_utilisationplan.get_by_interval(time_min character varying, time_max character varying, excluded_caseids character varying[], active boolean) OWNER TO postgres;

--
-- TOC entry 37 (class 1255 OID 17183)
-- Dependencies: 348 6
-- Name: get_nodes(character varying, character varying, character varying); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION get_nodes(activityname character varying, activitytype character varying, node character varying) RETURNS refcursor
    LANGUAGE plpgsql
    AS $_$
DECLARE
      p_ActivityName          alias for $1;
      p_ActivityType          alias for $2;
      p_Node                  alias for $3;
      
   c_UtilisationPlan CURSOR IS
      SELECT   a.case_id as case_id,
               (xpath('//Activity/'||p_Node, a.plan_))[1]::xml as nodexml
      FROM     utilisationplan a
      WHERE    (xpath('//Activity/ActivityName/text()', a.plan_))[1]::text = p_ActivityName and
               (xpath('//Activity/ActivityType/text()', a.plan_))[1]::text = p_ActivityType; 
--
BEGIN
  OPEN c_UtilisationPlan;
  RETURN c_UtilisationPlan;

EXCEPTION
   WHEN OTHERS THEN
      raise notice '% %', SQLSTATE, SQLERRM;    
END
$_$;


ALTER FUNCTION pkg_utilisationplan.get_nodes(activityname character varying, activitytype character varying, node character varying) OWNER TO postgres;

--
-- TOC entry 32 (class 1255 OID 17166)
-- Dependencies: 348 6
-- Name: save(character varying, character varying[], character varying, character varying, character varying, timestamp without time zone); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION save("CaseId" character varying, "UtilisationPlanParts" character varying[], "Name" character varying, "Description" character varying, "LastEditedBy" character varying, "LastEdited" timestamp without time zone) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
      p_CaseId                alias for $1;
      p_UtilisationPlanParts  alias for $2;
      p_Name                  alias for $3;
      p_Description           alias for $4;
      p_LastEditedBy          alias for $5;
      p_LastEdited            alias for $6;  
--
      i                       integer  :=1;
		v_DmlCount		         integer	:=1;
      v_Count			         integer;
      v_Plan			         XML;
   	v_Clob  			         text :='';
      v_RowCount              integer  :=0;    
BEGIN
  
   IF (p_UtilisationPlanParts is NULL) THEN
      RETURN v_RowCount;
   END IF;
   
   WHILE true LOOP
      IF (p_UtilisationPlanParts[i] is NULL) THEN 
         EXIT;
      END IF;
 		v_Clob := v_Clob || quote_literal(p_UtilisationPlanParts[i]);      
 		i := i +1;
   END LOOP;
    
   v_Plan:=XMLPARSE(CONTENT v_Clob);
   
   SELECT 	count(1)
   INTO		v_Count
	FROM		UTILISATIONPLAN
   WHERE		case_id=p_CaseId;
   
   BEGIN       
      IF (v_Count = 0) THEN
         INSERT 
         INTO    utilisationplan (
                     case_id, 
                     plan_,
                     name_, 
                     description, 
                     lastedited_by, 
                     lastedited)
         VALUES (
                     p_CaseId,
                     v_Plan,
                     p_Name,
                     p_Description,
                     p_LastEditedBy,
                     p_LastEdited);   
       ELSE
     	   UPDATE	UTILISATIONPLAN
         SET		plan_ = v_Plan,
                  name_= p_Name, 
                  description= p_Description, 
                  lastedited_by=p_LastEditedBy, 
                  lastedited=p_LastEdited
         WHERE	   case_id=p_CaseID;        
       END IF;
       GET DIAGNOSTICS v_RowCount = ROW_COUNT;
       return v_RowCount;
   EXCEPTION
      WHEN others THEN
         raise notice '% %', SQLSTATE, SQLERRM;
      BEGIN
         raise notice '% %', SQLSTATE, SQLERRM;
      END;
   END;
END;
$_$;


ALTER FUNCTION pkg_utilisationplan.save("CaseId" character varying, "UtilisationPlanParts" character varying[], "Name" character varying, "Description" character varying, "LastEditedBy" character varying, "LastEdited" timestamp without time zone) OWNER TO postgres;

--
-- TOC entry 38 (class 1255 OID 17215)
-- Dependencies: 348 6
-- Name: save_(character varying, xml, character varying, character varying, character varying, timestamp without time zone); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION save_("CaseId" character varying, "UtilisationPlan" xml, "Name" character varying, "Description" character varying, "LastEditedBy" character varying, "LastEdited" timestamp without time zone) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
      p_CaseId                alias for $1;
      p_Rup                   alias for $2;
      p_Name                  alias for $3;
      p_Description           alias for $4;
      p_LastEditedBy          alias for $5;
      p_LastEdited            alias for $6;  
--
      v_Count			         integer;
      v_RowCount              integer  :=0;    
BEGIN
    
   SELECT 	count(1)
   INTO		v_Count
	FROM		UTILISATIONPLAN
   WHERE		case_id=p_CaseId;
   
   BEGIN       
      IF (v_Count = 0) THEN
         INSERT 
         INTO    utilisationplan (
                     case_id, 
                     plan_,
                     name_, 
                     description, 
                     lastedited_by, 
                     lastedited)
         VALUES (
                     p_CaseId,
                     p_Rup,
                     p_Name,
                     p_Description,
                     p_LastEditedBy,
                     p_LastEdited); 
         --GET DIAGNOSTICS v_RowCount = ROW_COUNT;                       
       ELSE
     	   UPDATE	UTILISATIONPLAN
         SET		plan_ = p_Rup,
                  name_= p_Name, 
                  description= p_Description, 
                  lastedited_by=p_LastEditedBy, 
                  lastedited=p_LastEdited
         WHERE	   case_id=p_CaseID;
       --  GET DIAGNOSTICS v_RowCount = ROW_COUNT;        
       END IF;
       GET DIAGNOSTICS v_RowCount = ROW_COUNT; 
       return v_RowCount;
   EXCEPTION
      WHEN others THEN
         raise notice '% %', SQLSTATE, SQLERRM;
      BEGIN
         raise notice '% %', SQLSTATE, SQLERRM;
      END;
   END;
END;
$_$;


ALTER FUNCTION pkg_utilisationplan.save_("CaseId" character varying, "UtilisationPlan" xml, "Name" character varying, "Description" character varying, "LastEditedBy" character varying, "LastEdited" timestamp without time zone) OWNER TO postgres;

--
-- TOC entry 33 (class 1255 OID 17167)
-- Dependencies: 6 348
-- Name: update_status(character varying, integer); Type: FUNCTION; Schema: pkg_utilisationplan; Owner: postgres
--

CREATE FUNCTION update_status("CaseId" character varying, "Active" integer) RETURNS integer
    LANGUAGE plpgsql
    AS $_$
DECLARE
      p_CaseId                alias for $1;
      p_Active                alias for $2;
--
    	v_RowCount              integer  :=0;    
BEGIN
     
   BEGIN       
      UPDATE	UTILISATIONPLAN
      SET		active = p_Active
      WHERE	   case_id=p_CaseID;        
      GET DIAGNOSTICS v_RowCount = ROW_COUNT;
      return v_RowCount;
   EXCEPTION
      WHEN others THEN
         raise notice '% %', SQLSTATE, SQLERRM;
      BEGIN
         raise notice '% %', SQLSTATE, SQLERRM;
      END;
   END;
END;
$_$;


ALTER FUNCTION pkg_utilisationplan.update_status("CaseId" character varying, "Active" integer) OWNER TO postgres;

SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 1533 (class 1259 OID 16979)
-- Dependencies: 3
-- Name: mapping; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE mapping (
    workitemid character varying NOT NULL,
    requestkey integer,
    islocked character varying NOT NULL,
    workitemstatus character varying
);


ALTER TABLE public.mapping OWNER TO postgres;

--
-- TOC entry 1526 (class 1259 OID 16908)
-- Dependencies: 3
-- Name: qrtz_blob_triggers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_blob_triggers (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    blob_data bytea
);


ALTER TABLE public.qrtz_blob_triggers OWNER TO postgres;

--
-- TOC entry 1528 (class 1259 OID 16934)
-- Dependencies: 3
-- Name: qrtz_calendars; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_calendars (
    calendar_name character varying(200) NOT NULL,
    calendar bytea NOT NULL
);


ALTER TABLE public.qrtz_calendars OWNER TO postgres;

--
-- TOC entry 1525 (class 1259 OID 16895)
-- Dependencies: 3
-- Name: qrtz_cron_triggers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_cron_triggers (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    cron_expression character varying(120) NOT NULL,
    time_zone_id character varying(80)
);


ALTER TABLE public.qrtz_cron_triggers OWNER TO postgres;

--
-- TOC entry 1530 (class 1259 OID 16947)
-- Dependencies: 3
-- Name: qrtz_fired_triggers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_fired_triggers (
    entry_id character varying(95) NOT NULL,
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    is_volatile boolean NOT NULL,
    instance_name character varying(200) NOT NULL,
    fired_time bigint NOT NULL,
    priority integer NOT NULL,
    state character varying(16) NOT NULL,
    job_name character varying(200),
    job_group character varying(200),
    is_stateful boolean,
    requests_recovery boolean
);


ALTER TABLE public.qrtz_fired_triggers OWNER TO postgres;

--
-- TOC entry 1521 (class 1259 OID 16851)
-- Dependencies: 3
-- Name: qrtz_job_details; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_job_details (
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    description character varying(250),
    job_class_name character varying(250) NOT NULL,
    is_durable boolean NOT NULL,
    is_volatile boolean NOT NULL,
    is_stateful boolean NOT NULL,
    requests_recovery boolean NOT NULL,
    job_data bytea
);


ALTER TABLE public.qrtz_job_details OWNER TO postgres;

--
-- TOC entry 1522 (class 1259 OID 16859)
-- Dependencies: 3
-- Name: qrtz_job_listeners; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_job_listeners (
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    job_listener character varying(200) NOT NULL
);


ALTER TABLE public.qrtz_job_listeners OWNER TO postgres;

--
-- TOC entry 1532 (class 1259 OID 16960)
-- Dependencies: 3
-- Name: qrtz_locks; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_locks (
    lock_name character varying(40) NOT NULL
);


ALTER TABLE public.qrtz_locks OWNER TO postgres;

--
-- TOC entry 1529 (class 1259 OID 16942)
-- Dependencies: 3
-- Name: qrtz_paused_trigger_grps; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_paused_trigger_grps (
    trigger_group character varying(200) NOT NULL
);


ALTER TABLE public.qrtz_paused_trigger_grps OWNER TO postgres;

--
-- TOC entry 1531 (class 1259 OID 16955)
-- Dependencies: 3
-- Name: qrtz_scheduler_state; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_scheduler_state (
    instance_name character varying(200) NOT NULL,
    last_checkin_time bigint NOT NULL,
    checkin_interval bigint NOT NULL
);


ALTER TABLE public.qrtz_scheduler_state OWNER TO postgres;

--
-- TOC entry 1524 (class 1259 OID 16885)
-- Dependencies: 3
-- Name: qrtz_simple_triggers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_simple_triggers (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    repeat_count bigint NOT NULL,
    repeat_interval bigint NOT NULL,
    times_triggered bigint NOT NULL
);


ALTER TABLE public.qrtz_simple_triggers OWNER TO postgres;

--
-- TOC entry 1527 (class 1259 OID 16921)
-- Dependencies: 3
-- Name: qrtz_trigger_listeners; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_trigger_listeners (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    trigger_listener character varying(200) NOT NULL
);


ALTER TABLE public.qrtz_trigger_listeners OWNER TO postgres;

--
-- TOC entry 1523 (class 1259 OID 16872)
-- Dependencies: 3
-- Name: qrtz_triggers; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE qrtz_triggers (
    trigger_name character varying(200) NOT NULL,
    trigger_group character varying(200) NOT NULL,
    job_name character varying(200) NOT NULL,
    job_group character varying(200) NOT NULL,
    is_volatile boolean NOT NULL,
    description character varying(250),
    next_fire_time bigint,
    prev_fire_time bigint,
    priority integer,
    trigger_state character varying(16) NOT NULL,
    trigger_type character varying(8) NOT NULL,
    start_time bigint NOT NULL,
    end_time bigint,
    calendar_name character varying(200),
    misfire_instr smallint,
    job_data bytea
);


ALTER TABLE public.qrtz_triggers OWNER TO postgres;

SET default_with_oids = true;

--
-- TOC entry 1534 (class 1259 OID 16985)
-- Dependencies: 1801 3
-- Name: utilisationplan; Type: TABLE; Schema: public; Owner: postgres; Tablespace: 
--

CREATE TABLE utilisationplan (
    case_id character varying(255) NOT NULL,
    plan_ xml,
    name_ character varying(50),
    description character varying(1000),
    lastedited_by character varying(30),
    lastedited timestamp without time zone,
    active integer DEFAULT 1 NOT NULL
);


ALTER TABLE public.utilisationplan OWNER TO postgres;

--
-- TOC entry 1818 (class 2606 OID 16915)
-- Dependencies: 1526 1526 1526
-- Name: qrtz_blob_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);


--
-- TOC entry 1822 (class 2606 OID 16941)
-- Dependencies: 1528 1528
-- Name: qrtz_calendars_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_calendars
    ADD CONSTRAINT qrtz_calendars_pkey PRIMARY KEY (calendar_name);


--
-- TOC entry 1816 (class 2606 OID 16902)
-- Dependencies: 1525 1525 1525
-- Name: qrtz_cron_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);


--
-- TOC entry 1835 (class 2606 OID 16954)
-- Dependencies: 1530 1530
-- Name: qrtz_fired_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_fired_triggers
    ADD CONSTRAINT qrtz_fired_triggers_pkey PRIMARY KEY (entry_id);


--
-- TOC entry 1804 (class 2606 OID 16858)
-- Dependencies: 1521 1521 1521
-- Name: qrtz_job_details_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_job_details
    ADD CONSTRAINT qrtz_job_details_pkey PRIMARY KEY (job_name, job_group);


--
-- TOC entry 1806 (class 2606 OID 16866)
-- Dependencies: 1522 1522 1522 1522
-- Name: qrtz_job_listeners_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT qrtz_job_listeners_pkey PRIMARY KEY (job_name, job_group, job_listener);


--
-- TOC entry 1839 (class 2606 OID 16964)
-- Dependencies: 1532 1532
-- Name: qrtz_locks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_locks
    ADD CONSTRAINT qrtz_locks_pkey PRIMARY KEY (lock_name);


--
-- TOC entry 1824 (class 2606 OID 16946)
-- Dependencies: 1529 1529
-- Name: qrtz_paused_trigger_grps_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_paused_trigger_grps
    ADD CONSTRAINT qrtz_paused_trigger_grps_pkey PRIMARY KEY (trigger_group);


--
-- TOC entry 1837 (class 2606 OID 16959)
-- Dependencies: 1531 1531
-- Name: qrtz_scheduler_state_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_scheduler_state
    ADD CONSTRAINT qrtz_scheduler_state_pkey PRIMARY KEY (instance_name);


--
-- TOC entry 1814 (class 2606 OID 16889)
-- Dependencies: 1524 1524 1524
-- Name: qrtz_simple_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);


--
-- TOC entry 1820 (class 2606 OID 16928)
-- Dependencies: 1527 1527 1527 1527
-- Name: qrtz_trigger_listeners_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT qrtz_trigger_listeners_pkey PRIMARY KEY (trigger_name, trigger_group, trigger_listener);


--
-- TOC entry 1812 (class 2606 OID 16879)
-- Dependencies: 1523 1523 1523
-- Name: qrtz_triggers_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_pkey PRIMARY KEY (trigger_name, trigger_group);


--
-- TOC entry 1841 (class 2606 OID 17020)
-- Dependencies: 1534 1534
-- Name: utilisationplan_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres; Tablespace: 
--

ALTER TABLE ONLY utilisationplan
    ADD CONSTRAINT utilisationplan_pkey PRIMARY KEY (case_id);


--
-- TOC entry 1825 (class 1259 OID 16976)
-- Dependencies: 1530
-- Name: idx_qrtz_ft_job_group; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_job_group ON qrtz_fired_triggers USING btree (job_group);


--
-- TOC entry 1826 (class 1259 OID 16975)
-- Dependencies: 1530
-- Name: idx_qrtz_ft_job_name; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_job_name ON qrtz_fired_triggers USING btree (job_name);


--
-- TOC entry 1827 (class 1259 OID 16978)
-- Dependencies: 1530
-- Name: idx_qrtz_ft_job_req_recovery; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_job_req_recovery ON qrtz_fired_triggers USING btree (requests_recovery);


--
-- TOC entry 1828 (class 1259 OID 16977)
-- Dependencies: 1530
-- Name: idx_qrtz_ft_job_stateful; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_job_stateful ON qrtz_fired_triggers USING btree (is_stateful);


--
-- TOC entry 1829 (class 1259 OID 16971)
-- Dependencies: 1530
-- Name: idx_qrtz_ft_trig_group; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_trig_group ON qrtz_fired_triggers USING btree (trigger_group);


--
-- TOC entry 1830 (class 1259 OID 16974)
-- Dependencies: 1530
-- Name: idx_qrtz_ft_trig_inst_name; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_trig_inst_name ON qrtz_fired_triggers USING btree (instance_name);


--
-- TOC entry 1831 (class 1259 OID 16970)
-- Dependencies: 1530
-- Name: idx_qrtz_ft_trig_name; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_trig_name ON qrtz_fired_triggers USING btree (trigger_name);


--
-- TOC entry 1832 (class 1259 OID 16972)
-- Dependencies: 1530 1530
-- Name: idx_qrtz_ft_trig_nm_gp; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_trig_nm_gp ON qrtz_fired_triggers USING btree (trigger_name, trigger_group);


--
-- TOC entry 1833 (class 1259 OID 16973)
-- Dependencies: 1530
-- Name: idx_qrtz_ft_trig_volatile; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_ft_trig_volatile ON qrtz_fired_triggers USING btree (is_volatile);


--
-- TOC entry 1802 (class 1259 OID 16965)
-- Dependencies: 1521
-- Name: idx_qrtz_j_req_recovery; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_j_req_recovery ON qrtz_job_details USING btree (requests_recovery);


--
-- TOC entry 1807 (class 1259 OID 16966)
-- Dependencies: 1523
-- Name: idx_qrtz_t_next_fire_time; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_t_next_fire_time ON qrtz_triggers USING btree (next_fire_time);


--
-- TOC entry 1808 (class 1259 OID 16968)
-- Dependencies: 1523 1523
-- Name: idx_qrtz_t_nft_st; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_t_nft_st ON qrtz_triggers USING btree (next_fire_time, trigger_state);


--
-- TOC entry 1809 (class 1259 OID 16967)
-- Dependencies: 1523
-- Name: idx_qrtz_t_state; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_t_state ON qrtz_triggers USING btree (trigger_state);


--
-- TOC entry 1810 (class 1259 OID 16969)
-- Dependencies: 1523
-- Name: idx_qrtz_t_volatile; Type: INDEX; Schema: public; Owner: postgres; Tablespace: 
--

CREATE INDEX idx_qrtz_t_volatile ON qrtz_triggers USING btree (is_volatile);


--
-- TOC entry 1846 (class 2606 OID 16916)
-- Dependencies: 1523 1811 1526 1526 1523
-- Name: qrtz_blob_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_blob_triggers
    ADD CONSTRAINT qrtz_blob_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 1845 (class 2606 OID 16903)
-- Dependencies: 1525 1523 1523 1525 1811
-- Name: qrtz_cron_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_cron_triggers
    ADD CONSTRAINT qrtz_cron_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 1842 (class 2606 OID 16867)
-- Dependencies: 1521 1803 1522 1522 1521
-- Name: qrtz_job_listeners_job_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_job_listeners
    ADD CONSTRAINT qrtz_job_listeners_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);


--
-- TOC entry 1844 (class 2606 OID 16890)
-- Dependencies: 1811 1524 1524 1523 1523
-- Name: qrtz_simple_triggers_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_simple_triggers
    ADD CONSTRAINT qrtz_simple_triggers_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 1847 (class 2606 OID 16929)
-- Dependencies: 1811 1523 1523 1527 1527
-- Name: qrtz_trigger_listeners_trigger_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_trigger_listeners
    ADD CONSTRAINT qrtz_trigger_listeners_trigger_name_fkey FOREIGN KEY (trigger_name, trigger_group) REFERENCES qrtz_triggers(trigger_name, trigger_group);


--
-- TOC entry 1843 (class 2606 OID 16880)
-- Dependencies: 1523 1803 1521 1521 1523
-- Name: qrtz_triggers_job_name_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY qrtz_triggers
    ADD CONSTRAINT qrtz_triggers_job_name_fkey FOREIGN KEY (job_name, job_group) REFERENCES qrtz_job_details(job_name, job_group);


--
-- TOC entry 1853 (class 0 OID 0)
-- Dependencies: 3
-- Name: public; Type: ACL; Schema: -; Owner: postgres
--

REVOKE ALL ON SCHEMA public FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM postgres;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO PUBLIC;


-- Completed on 2011-09-02 18:23:07

--
-- PostgreSQL database dump complete
--

