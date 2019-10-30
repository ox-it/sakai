--
-- SAK-31840 drop defaults as its now managed in the POJO
--
ALTER TABLE GB_GRADABLE_OBJECT_T MODIFY IS_EXTRA_CREDIT DEFAULT NULL;
ALTER TABLE GB_GRADABLE_OBJECT_T MODIFY HIDE_IN_ALL_GRADES_TABLE DEFAULT NULL;
-- SAM-3012 - Update samigo events
-- Update camel case events
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit' WHERE EVENT = 'sam.assessmentSubmitted';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.graded.auto' WHERE EVENT = 'sam.assessmentAutoGraded';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.auto' WHERE EVENT = 'sam.assessmentAutoSubmitted';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.timer.thrd' WHERE EVENT = 'sam.assessmentTimedSubmitted';
UPDATE SAKAI_EVENT SET EVENT = 'sam.pubassessment.remove' WHERE EVENT = 'sam.pubAssessment.remove';

-- Update name of submission events
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.from_last' WHERE EVENT = 'sam.submit.from_last_page';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.from_toc' WHERE EVENT = 'sam.submit.from_toc';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.thread' WHERE EVENT = 'sam.assessment.thread_submit';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.timer' WHERE EVENT = 'sam.assessment.timer_submit';
UPDATE SAKAI_EVENT SET EVENT = 'sam.assessment.submit.timer.url' WHERE EVENT = 'sam.assessment.timer_submit.url';

-- END SAM-3012
-- After running these conversions and starting your system you must run the Assignment Conversion Job or you will not have assignments.
-- Please see https://github.com/sakaiproject/sakai/blob/12.x/assignment/impl/src/java/org/sakaiproject/assignment/impl/conversion/README.md
-- For additional information!

-- SAM-3016
ALTER TABLE SAM_EVENTLOG_T ADD IPADDRESS varchar2(99);

-- SAK-30207
/*CREATE TABLE CONTENTREVIEW_ITEM (
    ID                  NUMBER(19) NOT NULL,
    VERSION             INTEGER NOT NULL,
    PROVIDERID          INTEGER NOT NULL,
    CONTENTID           VARCHAR2(255) NOT NULL,
    USERID              VARCHAR2(255),
    SITEID              VARCHAR2(255),
    TASKID              VARCHAR2(255),
    EXTERNALID          VARCHAR2(255),
    DATEQUEUED          TIMESTAMP NOT NULL,
    DATESUBMITTED       TIMESTAMP,
    DATEREPORTRECEIVED  TIMESTAMP,
    STATUS              NUMBER(19),
    REVIEWSCORE         INTEGER,
    LASTERROR           CLOB,
    RETRYCOUNT          NUMBER(19),
    NEXTRETRYTIME       TIMESTAMP NOT NULL,
    ERRORCODE           INTEGER,
    CONSTRAINT ID PRIMARY KEY (ID),
    CONSTRAINT PROVIDERID UNIQUE (PROVIDERID, CONTENTID)
);*/
-- END SAK-30207

-- Alterations for OWL:
-- alter table to add the version, providerId
ALTER TABLE CONTENTREVIEW_ITEM ADD VERSION INTEGER default 0 NOT NULL;
-- Use an appropriate default. -1 since everything existing will stop working
ALTER TABLE CONTENTREVIEW_ITEM ADD PROVIDERID INTEGER default -1 NOT NULL;
-- TODO: Eventually drop URLACCESSED, SUBMISSIONID, RESUBMISSION, EXTERNALGRADE, but for now let them be null
ALTER TABLE CONTENTREVIEW_ITEM MODIFY (URLACCESSED NULL);
-- SUBMISSIONID already allows null
ALTER TABLE CONTENTREVIEW_ITEM MODIFY (EXTERNALGRADE NULL);
-- EXTERNALGRADE already allows null

-- SAK-33723 Content review item properties
CREATE TABLE CONTENTREVIEW_ITEM_PROPERTIES (
  CONTENTREVIEW_ITEM_ID number(19) NOT NULL,
  VALUE varchar2(255) DEFAULT NULL,
  PROPERTY varchar2(255) NOT NULL,
  PRIMARY KEY (CONTENTREVIEW_ITEM_ID,PROPERTY),
  FOREIGN KEY (CONTENTREVIEW_ITEM_ID) REFERENCES CONTENTREVIEW_ITEM (id)
);

-- END SAK-33723

--
-- SAK-31641 Switch from INTs to VARCHARs in Oauth
--
ALTER TABLE oauth_accessors
MODIFY (
  status VARCHAR2(255)
, type VARCHAR2(255)
);

UPDATE oauth_accessors SET status = CASE
  WHEN status = 0 THEN 'VALID'
  WHEN status = 1 THEN 'REVOKED'
  WHEN status = 2 THEN 'EXPIRED'
END;

UPDATE oauth_accessors SET type = CASE
  WHEN type = 0 THEN 'REQUEST'
  WHEN type = 1 THEN 'REQUEST_AUTHORISING'
  WHEN type = 2 THEN 'REQUEST_AUTHORISED'
  WHEN type = 3 THEN 'ACCESS'
END;

--
-- SAK-31636 Rename existing 'Home' tools
--

update sakai_site_page set title = 'Overview' where title = 'Home';

--
-- SAK-31563
--

-- Add new user_id columns and their corresponding indexes
ALTER TABLE pasystem_popup_assign ADD user_id varchar2(99);
ALTER TABLE pasystem_popup_dismissed ADD user_id varchar2(99);
ALTER TABLE pasystem_banner_dismissed ADD user_id varchar2(99);

CREATE INDEX popup_assign_lower_user_id on pasystem_popup_assign (user_id);
CREATE INDEX popup_dismissed_lower_user_id on pasystem_popup_dismissed (user_id);
CREATE INDEX banner_dismissed_user_id on pasystem_banner_dismissed (user_id);

-- Map existing EIDs to their corresponding user IDs
update pasystem_popup_assign popup set user_id = (select user_id from sakai_user_id_map map where popup.user_eid = map.eid);
update pasystem_popup_dismissed popup set user_id = (select user_id from sakai_user_id_map map where popup.user_eid = map.eid);
update pasystem_banner_dismissed banner set user_id = (select user_id from sakai_user_id_map map where banner.user_eid = map.eid);

-- Any rows that couldn't be mapped are dropped (there shouldn't
-- really be any, but if there are those users were already being
-- ignored when identified by EID)
DELETE FROM pasystem_popup_assign WHERE user_id is null;
DELETE FROM pasystem_popup_dismissed WHERE user_id is null;
DELETE FROM pasystem_banner_dismissed WHERE user_id is null;

-- Enforce NULL checks on the new columns
ALTER TABLE pasystem_popup_assign MODIFY user_id varchar2(99) NOT NULL;
ALTER TABLE pasystem_popup_dismissed MODIFY user_id varchar2(99) NOT NULL;
ALTER TABLE pasystem_banner_dismissed MODIFY user_id varchar2(99) NOT NULL;

-- Reintroduce unique constraints for the new column
ALTER TABLE pasystem_popup_dismissed drop CONSTRAINT popup_dismissed_unique;
ALTER TABLE pasystem_popup_dismissed add CONSTRAINT popup_dismissed_unique UNIQUE (user_id, state, uuid);

ALTER TABLE pasystem_banner_dismissed drop CONSTRAINT banner_dismissed_unique;
ALTER TABLE pasystem_banner_dismissed add CONSTRAINT banner_dismissed_unique UNIQUE (user_id, state, uuid);

-- Drop the old columns
ALTER TABLE pasystem_popup_assign DROP COLUMN user_eid;
ALTER TABLE pasystem_popup_dismissed DROP COLUMN user_eid;
ALTER TABLE pasystem_banner_dismissed DROP COLUMN user_eid;

--
-- SAK-31840 drop defaults as its now managed in the POJO
--
ALTER TABLE GB_GRADABLE_OBJECT_T MODIFY IS_EXTRA_CREDIT number(1) DEFAULT NULL;
ALTER TABLE GB_GRADABLE_OBJECT_T MODIFY HIDE_IN_ALL_GRADES_TABLE number(1) DEFAULT NULL;

--LSNBLDR-633 Restrict editing of Lessons pages and subpages to one person
ALTER TABLE lesson_builder_pages ADD owned number(1) default 0 not null;
-- END LSNBLDR-633

-- BEGIN SAK-31819 Remove the old ScheduledInvocationManager job as it's not present in Sakai 12.
DELETE FROM QRTZ_SIMPLE_TRIGGERS WHERE TRIGGER_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
DELETE FROM QRTZ_TRIGGERS WHERE TRIGGER_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
-- This one is the actual job that the triggers were trying to run
DELETE FROM QRTZ_JOB_DETAILS WHERE JOB_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
-- END SAK-31819

-- BEGIN SAK-15708 avoid duplicate rows
-- Brian's note: Shouldn't be necessary, but in case we missed something and we end up with a duplicate between now and outs20. The index stuff below is still needed.
CREATE TABLE SAKAI_POSTEM_STUDENT_DUPES (
  id number not null,
  username varchar2(99),
  surrogate_key number
);
INSERT INTO SAKAI_POSTEM_STUDENT_DUPES SELECT MAX(id), username, surrogate_key FROM SAKAI_POSTEM_STUDENT GROUP BY username, surrogate_key HAVING count(id) > 1;
DELETE FROM SAKAI_POSTEM_STUDENT_GRADES WHERE student_id IN (SELECT id FROM SAKAI_POSTEM_STUDENT_DUPES);
DELETE FROM SAKAI_POSTEM_STUDENT WHERE id IN (SELECT id FROM SAKAI_POSTEM_STUDENT_DUPES);
DROP TABLE SAKAI_POSTEM_STUDENT_DUPES;

DROP INDEX POSTEM_STUDENT_USERNAME_I;
ALTER TABLE SAKAI_POSTEM_STUDENT MODIFY ( "USERNAME" VARCHAR2(99 CHAR) ) ;
CREATE UNIQUE INDEX POSTEM_USERNAME_SURROGATE ON SAKAI_POSTEM_STUDENT ("USERNAME" ASC, "SURROGATE_KEY" ASC);
-- END SAK-15708

-- BEGIN SAK-32083 TAGS

CREATE TABLE tagservice_collection (
  tagcollectionid VARCHAR2(36) PRIMARY KEY,
  description CLOB,
  externalsourcename VARCHAR2(255),
  externalsourcedescription CLOB,
  name VARCHAR2(255),
  createdby VARCHAR2(255),
  creationdate NUMBER,
  lastmodifiedby VARCHAR2(255),
  lastmodificationdate NUMBER,
  lastsynchronizationdate NUMBER,
  externalupdate NUMBER(1,0),
  externalcreation NUMBER(1,0),
  lastupdatedateinexternalsystem NUMBER,
  CONSTRAINT externalsourcename_UNIQUE UNIQUE (externalsourcename),
  CONSTRAINT name_UNIQUE UNIQUE (name)
);

CREATE TABLE tagservice_tag (
  tagid VARCHAR2(36) PRIMARY KEY,
  tagcollectionid VARCHAR2(36) NOT NULL,
  externalid VARCHAR2(255),
  taglabel VARCHAR2(255),
  description CLOB,
  alternativelabels CLOB,
  createdby VARCHAR2(255),
  creationdate NUMBER,
  externalcreation NUMBER(1,0),
  externalcreationDate NUMBER,
  externalupdate NUMBER(1,0),
  lastmodifiedby VARCHAR2(255),
  lastmodificationdate NUMBER,
  lastupdatedateinexternalsystem NUMBER,
  parentid VARCHAR2(255),
  externalhierarchycode CLOB,
  externaltype VARCHAR2(255),
  data CLOB,
  CONSTRAINT tagservice_tag_fk FOREIGN KEY (tagcollectionid) REFERENCES tagservice_collection(tagcollectionid)
);


CREATE INDEX tagservice_tag_tagcollectionid on tagservice_tag (tagcollectionid);
CREATE INDEX tagservice_tag_taglabel on tagservice_tag (taglabel);
CREATE INDEX tagservice_tag_externalid on tagservice_tag (externalid);



MERGE INTO SAKAI_REALM_FUNCTION srf
USING (
SELECT -123 as function_key,
'tagservice.manage' as function_name
FROM dual
) t on (srf.function_name = t.function_name)
WHEN NOT MATCHED THEN
INSERT (function_key, function_name)
VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, t.function_name);

-- END SAK-32083 TAGS

-- Decided that 3432 is inappropriate for OWL
/*
-- BEGIN 3432 Grade Points Grading Scale
-- We have a few grading scales in production already; the registrar grades are the only ones accepted for course grade submission. So this isn't an issue --bbailla2
-- add the new grading scale
INSERT INTO gb_grading_scale_t (id, object_type_id, version, scale_uid, name, unavailable)
VALUES (gb_grading_scale_s.nextval, 0, 0, 'GradePointsMapping', 'Grade Points', 0);

-- add the grade ordering
INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'A (4.0)', 0);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'A- (3.67)', 1);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'B+ (3.33)', 2);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'B (3.0)', 3);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'B- (2.67)', 4);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'C+ (2.33)', 5);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'C (2.0)', 6);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'C- (1.67)', 7);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'D (1.0)', 8);

INSERT INTO gb_grading_scale_grades_t (grading_scale_id, letter_grade, grade_idx)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 'F (0)', 9);

-- add the percent mapping
INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 100, 'A (4.0)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 90, 'A- (3.67)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 87, 'B+ (3.33)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 83, 'B (3.0)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 80, 'B- (2.67)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 77, 'C+ (2.33)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 73, 'C (2.0)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 70, 'C- (1.67)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 67, 'D (1.0)');

INSERT INTO gb_grading_scale_percents_t (grading_scale_id, percent, letter_grade)
VALUES(
(SELECT id FROM gb_grading_scale_t WHERE scale_uid = 'GradePointsMapping')
, 0, 'F (0)');

-- add the new scale to all existing gradebook sites
INSERT INTO gb_grade_map_t (id, object_type_id, version, gradebook_id, gb_grading_scale_t)
SELECT 
  gb_grade_mapping_s.nextval
, 0
, 0
, gb.id
, gs.id
FROM gb_gradebook_t gb
JOIN gb_grading_scale_t gs
  ON gs.scale_uid = 'GradePointsMapping';
-- END 3432
*/

-- SAM-1129 Change the column DESCRIPTION of SAM_QUESTIONPOOL_T from VARCHAR2(255) to CLOB

ALTER TABLE SAM_QUESTIONPOOL_T ADD DESCRIPTION_COPY VARCHAR2(255);
UPDATE SAM_QUESTIONPOOL_T SET DESCRIPTION_COPY = DESCRIPTION;

UPDATE SAM_QUESTIONPOOL_T SET DESCRIPTION = NULL;
ALTER TABLE SAM_QUESTIONPOOL_T MODIFY DESCRIPTION LONG;
ALTER TABLE SAM_QUESTIONPOOL_T MODIFY DESCRIPTION CLOB;
UPDATE SAM_QUESTIONPOOL_T SET DESCRIPTION = DESCRIPTION_COPY;

ALTER TABLE SAM_QUESTIONPOOL_T DROP COLUMN DESCRIPTION_COPY;

-- SAK-30461 Portal bullhorns
CREATE TABLE BULLHORN_ALERTS
(
    ID NUMBER(19) NOT NULL,
    ALERT_TYPE VARCHAR(8) NOT NULL,
    FROM_USER VARCHAR2(99) NOT NULL,
    TO_USER VARCHAR2(99) NOT NULL,
    EVENT VARCHAR2(32) NOT NULL,
    REF VARCHAR2(255) NOT NULL,
    TITLE VARCHAR2(255),
    SITE_ID VARCHAR2(99),
    URL CLOB NOT NULL,
    EVENT_DATE TIMESTAMP NOT NULL,
    PRIMARY KEY(ID)
);

CREATE SEQUENCE bullhorn_alerts_seq;

CREATE OR REPLACE TRIGGER bullhorn_alerts_bir
    BEFORE INSERT ON BULLHORN_ALERTS
    FOR EACH ROW
    BEGIN
        SELECT bullhorn_alerts_seq.NEXTVAL
        INTO   :new.id
        FROM   dual;
    END;

-- SAK-32417 Forums permission composite index
CREATE INDEX MFR_COMPOSITE_PERM ON MFR_PERMISSION_LEVEL_T (TYPE_UUID, NAME);

-- SAK-32442 - LTI Column cleanup
-- These conversions may fail if you started Sakai at newer versions that didn't contain these columns/tables
alter table lti_tools drop column enabled_capability;
alter table lti_deploy drop column allowlori;
alter table lti_tools drop column allowlori;
drop table lti_mapping;
-- END SAK-32442

-- SAK-32572  SAK-33910 Additional permission settings for Messages
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'msg.permissions.allowToField.myGroupRoles');

--The permission above is false for all users by default
--if you want to turn this feature on for all "student/acces" type roles, then run 
--the following conversion:

-- Decision: Apply this on dev, test it out and discuss if it makes sense for us, then decide if we apply or not for qat / prd --bbailla2

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));



INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));


INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));

-- Added for OWL: Course Coordinator, Grade Admin, Secondary Instructor:

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));



--Note: this isn't the end of the backfill process, it continues below --bbailla2
-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permission into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('maintain','msg.permissions.allowToField.myGroupRoles');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('access','msg.permissions.allowToField.myGroupRoles');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Instructor','msg.permissions.allowToField.myGroupRoles');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Teaching Assistant','msg.permissions.allowToField.myGroupRoles');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Student','msg.permissions.allowToField.myGroupRoles');
-- Added for OWL:
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','msg.permissions.allowToField.myGroupRoles');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','msg.permissions.allowToField.myGroupRoles');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','msg.permissions.allowToField.myGroupRoles');

-- lookup the role and function number
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
FROM PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new function into the roles of any existing realm that has the role (don't convert the "!site.helper")
-- added the 'distinct' in the upper select --bbailla2
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    distinct SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

-- END SAK-32572 Additional permission settings for Messages

-- SAK-33430 user_audits_log is queried against site_id
ALTER TABLE user_audits_log MODIFY (site_id varchar2(99));
ALTER TABLE user_audits_log MODIFY (role_name varchar2(99));

-- Added this alter table, as the 'drop index' command breaks without it --bbailla2
ALTER TABLE user_audits_log DROP PRIMARY KEY DROP INDEX;
-- I suspect this is now unneeded --bbailla2
DROP INDEX user_audits_log_index;
CREATE INDEX user_audits_log_index on user_audits_log (site_id);
-- END SAK-33430

-- SAK-33406 - Allow reorder of LTI plugin tools
alter table lti_tools add toolorder NUMBER(11) DEFAULT '0';
alter table lti_content add toolorder NUMBER(11) DEFAULT '0';
-- END SAK-33406

-- SAK-33898
ALTER TABLE lti_content ADD sha256 NUMBER(2) DEFAULT '0';
ALTER TABLE lti_tools ADD sha256 NUMBER(2) DEFAULT '0';
-- END SAK-33898

-- SAK-32440 
alter table lti_tools add siteinfoconfig NUMBER(2) DEFAULT '0';
-- END SAK-32440

-- BEGIN SAK-32045 -- Update My Workspace to My Home
UPDATE SAKAI_SITE
SET TITLE = 'Home', DESCRIPTION = 'Home'
WHERE SITE_ID LIKE '!user%';

UPDATE SAKAI_SITE
SET TITLE = 'Home', DESCRIPTION = 'Home'
WHERE TITLE = 'My Workspace'
AND SITE_ID LIKE '~%';

UPDATE SAKAI_SITE_TOOL
SET TITLE = 'Home' 
WHERE REGISTRATION = 'sakai.iframe.myworkspace';
-- END SAK-32045

--
-- BEGIN NEW ASSIGNMENTS TABLES
--

CREATE TABLE ASN_ASSIGNMENT (
  ASSIGNMENT_ID varchar2(36) NOT NULL,
  ALLOW_ATTACHMENTS number(1) DEFAULT NULL,
  ALLOW_PEER_ASSESSMENT number(1) DEFAULT NULL,
  AUTHOR varchar2(99) DEFAULT NULL,
  CLOSE_DATE timestamp DEFAULT NULL,
  CONTENT_REVIEW number(1) DEFAULT NULL,
  CONTEXT varchar2(99) NOT NULL,
  CREATED_DATE timestamp NOT NULL,
  MODIFIED_DATE timestamp DEFAULT NULL,
  DELETED number(1) DEFAULT NULL,
  DRAFT number(1) NOT NULL,
  DROP_DEAD_DATE timestamp DEFAULT NULL,
  DUE_DATE timestamp DEFAULT NULL,
  HIDE_DUE_DATE number(1) DEFAULT NULL,
  HONOR_PLEDGE number(1) DEFAULT NULL,
  INDIVIDUALLY_GRADED number(1) DEFAULT NULL,
  INSTRUCTIONS clob,
  IS_GROUP number(1) DEFAULT NULL,
  MAX_GRADE_POINT integer DEFAULT NULL,
  MODIFIER varchar2(99) DEFAULT NULL,
  OPEN_DATE timestamp DEFAULT NULL,
  PEER_ASSESSMENT_ANON_EVAL number(1) DEFAULT NULL,
  PEER_ASSESSMENT_INSTRUCTIONS clob,
  PEER_ASSESSMENT_NUMBER_REVIEW integer DEFAULT NULL,
  PEER_ASSESSMENT_PERIOD_DATE timestamp DEFAULT NULL,
  PEER_ASSESSMENT_STUDENT_REVIEW number(1) DEFAULT NULL,
  POSITION integer DEFAULT NULL,
  RELEASE_GRADES number(1) DEFAULT NULL,
  SCALE_FACTOR integer DEFAULT NULL,
  SECTION varchar2(255) DEFAULT NULL,
  TITLE varchar2(255) DEFAULT NULL,
  ACCESS_TYPE varchar2(255) NOT NULL,
  GRADE_TYPE integer DEFAULT NULL,
  SUBMISSION_TYPE integer DEFAULT NULL,
  VISIBLE_DATE timestamp DEFAULT NULL,
  PRIMARY KEY (ASSIGNMENT_ID)
);

CREATE TABLE ASN_ASSIGNMENT_ATTACHMENTS (
  ASSIGNMENT_ID varchar2(36) NOT NULL,
  ATTACHMENT varchar2(1024) DEFAULT NULL,
  CONSTRAINT FK_ASN_ASSIGNMENT_ATT FOREIGN KEY (ASSIGNMENT_ID) REFERENCES ASN_ASSIGNMENT (ASSIGNMENT_ID)
);

CREATE TABLE ASN_ASSIGNMENT_GROUPS (
  ASSIGNMENT_ID varchar2(36) NOT NULL,
  GROUP_ID varchar2(255) DEFAULT NULL,
  CONSTRAINT FK_ASN_ASSIGNMENTS_GRP FOREIGN KEY (ASSIGNMENT_ID) REFERENCES ASN_ASSIGNMENT (ASSIGNMENT_ID)
);

CREATE TABLE ASN_ASSIGNMENT_PROPERTIES (
  ASSIGNMENT_ID varchar2(36) NOT NULL,
  VALUE clob DEFAULT NULL,
  NAME varchar2(255) NOT NULL,
  PRIMARY KEY (ASSIGNMENT_ID,NAME),
  CONSTRAINT FK_ASN_ASSIGMENTS_PROP FOREIGN KEY (ASSIGNMENT_ID) REFERENCES ASN_ASSIGNMENT (ASSIGNMENT_ID)
);

CREATE TABLE ASN_SUBMISSION (
  SUBMISSION_ID varchar2(36) NOT NULL,
  CREATED_DATE timestamp DEFAULT NULL,
  MODIFIED_DATE timestamp DEFAULT NULL,
  RETURNED_DATE timestamp DEFAULT NULL,
  SUBMITTED_DATE timestamp DEFAULT NULL,
  FACTOR integer DEFAULT NULL,
  FEEDBACK_COMMENT clob,
  FEEDBACK_TEXT clob,
  GRADE varchar2(32) DEFAULT NULL,
  GRADE_RELEASED number(1) DEFAULT NULL,
  GRADED number(1) DEFAULT NULL,
  GRADED_BY varchar2(99) DEFAULT NULL,
  GROUP_ID varchar2(36) DEFAULT NULL,
  HIDDEN_DUE_DATE number(1) DEFAULT NULL,
  HONOR_PLEDGE number(1) DEFAULT NULL,
  RETURNED number(1) DEFAULT NULL,
  SUBMITTED number(1) DEFAULT NULL,
  TEXT clob,
  USER_SUBMISSION number(1) DEFAULT NULL,
  ASSIGNMENT_ID varchar2(36) DEFAULT NULL,
  PRIMARY KEY (SUBMISSION_ID),
  CONSTRAINT FK_ASN_ASSIGMENTS_SUB FOREIGN KEY (ASSIGNMENT_ID) REFERENCES ASN_ASSIGNMENT (ASSIGNMENT_ID)
);

CREATE TABLE ASN_SUBMISSION_ATTACHMENTS (
  SUBMISSION_ID varchar2(36) NOT NULL,
  ATTACHMENT varchar2(1024) DEFAULT NULL,
  CONSTRAINT FK_ASN_SUBMISSION_ATT FOREIGN KEY (SUBMISSION_ID) REFERENCES ASN_SUBMISSION (SUBMISSION_ID)
);

CREATE TABLE ASN_SUBMISSION_FEEDBACK_ATTACH (
  SUBMISSION_ID varchar2(36) NOT NULL,
  FEEDBACK_ATTACHMENT varchar2(1024) DEFAULT NULL,
  CONSTRAINT FK_ASN_SUBMISSION_FEE FOREIGN KEY (SUBMISSION_ID) REFERENCES ASN_SUBMISSION (SUBMISSION_ID)
);

CREATE TABLE ASN_SUBMISSION_PROPERTIES (
  SUBMISSION_ID varchar2(36) NOT NULL,
  VALUE clob DEFAULT NULL,
  NAME varchar2(255) NOT NULL,
  PRIMARY KEY (SUBMISSION_ID,NAME),
  CONSTRAINT FK_ASN_SUBMISSION_PROP FOREIGN KEY (SUBMISSION_ID) REFERENCES ASN_SUBMISSION (SUBMISSION_ID)
);

CREATE TABLE ASN_SUBMISSION_SUBMITTER (
  ID number NOT NULL,
  FEEDBACK clob,
  GRADE varchar2(32) DEFAULT NULL,
  SUBMITTEE number(1) NOT NULL,
  SUBMITTER varchar2(99) NOT NULL,
  SUBMISSION_ID varchar2(36) NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT UK_ASN_SUBMISSION UNIQUE (SUBMISSION_ID,SUBMITTER),
  CONSTRAINT FK_ASN_SUBMISSION_SUB FOREIGN KEY (SUBMISSION_ID) REFERENCES ASN_SUBMISSION (SUBMISSION_ID)
);

CREATE SEQUENCE ASN_SUBMISSION_SUBMITTERS_S;

--
-- END NEW ASSIGNMENTS TABLES
--

-- SAK-32642 Commons Tools

CREATE TABLE COMMONS_COMMENT (
  ID varchar2(36) NOT NULL,
  POST_ID varchar2(36) DEFAULT NULL,
  CONTENT clob NOT NULL,
  CREATOR_ID varchar2(99) NOT NULL,
  CREATED_DATE timestamp NOT NULL,
  MODIFIED_DATE timestamp NOT NULL,
  PRIMARY KEY (ID)
);

CREATE INDEX IDX_COMMONS_CREATOR ON COMMONS_COMMENT(CREATOR_ID);
CREATE INDEX IDX_COMMONS_POST ON COMMONS_COMMENT(POST_ID);

CREATE TABLE COMMONS_COMMONS (
  ID varchar2(36) NOT NULL,
  SITE_ID varchar2(99) NOT NULL,
  EMBEDDER varchar2(24) NOT NULL,
  PRIMARY KEY (ID)
);

CREATE TABLE COMMONS_COMMONS_POST (
  COMMONS_ID varchar2(36) DEFAULT NULL,
  POST_ID varchar2(36) DEFAULT NULL,
  CONSTRAINT UK_COMMONS_ID_POST_ID UNIQUE (COMMONS_ID,POST_ID)
);

CREATE TABLE COMMONS_POST (
  ID varchar2(36) NOT NULL,
  CONTENT clob NOT NULL,
  CREATOR_ID varchar2(99) NOT NULL,
  CREATED_DATE timestamp NOT NULL,
  MODIFIED_DATE timestamp NOT NULL,
  RELEASE_DATE timestamp NOT NULL,
  PRIMARY KEY (ID)
);

CREATE INDEX IDX_COMMONS_POST_CREATOR ON COMMONS_POST(CREATOR_ID);

-- END SAK-32642

-- SAM-2970 Extended Time

CREATE TABLE SAM_EXTENDEDTIME_T (
  ID number NOT NULL,
  ASSESSMENT_ID number DEFAULT NULL,
  PUB_ASSESSMENT_ID number DEFAULT NULL,
  USER_ID varchar2(255) DEFAULT NULL,
  GROUP_ID varchar2(255) DEFAULT NULL,
  START_DATE timestamp DEFAULT NULL,
  DUE_DATE timestamp DEFAULT NULL,
  RETRACT_DATE timestamp DEFAULT NULL,
  TIME_HOURS integer DEFAULT NULL,
  TIME_MINUTES integer DEFAULT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT FK_EXTENDEDTIME_PUBASSESSMENT FOREIGN KEY (PUB_ASSESSMENT_ID) REFERENCES SAM_PUBLISHEDASSESSMENT_T (ID),
  CONSTRAINT FK_EXTENDEDTIME_ASSESMENTBASE FOREIGN KEY (ASSESSMENT_ID) REFERENCES SAM_ASSESSMENTBASE_T (ID)
);

CREATE INDEX IDX_EXTENDEDTIME_ASSESMENT_ID ON SAM_EXTENDEDTIME_T(ASSESSMENT_ID);
CREATE INDEX IDX_EXTENDEDTIME_ASSESMENT_PID ON SAM_EXTENDEDTIME_T(PUB_ASSESSMENT_ID);

CREATE SEQUENCE SAM_EXTENDEDTIME_S;

-- END SAM-2970


-- SAM-3115 Tags and Search in Samigo

ALTER TABLE SAM_ITEM_T ADD HASH varchar(255) DEFAULT NULL;
ALTER TABLE SAM_PUBLISHEDITEM_T ADD HASH varchar(255) DEFAULT NULL;
ALTER TABLE SAM_PUBLISHEDITEM_T ADD ITEMHASH varchar(255) DEFAULT NULL;

CREATE TABLE SAM_ITEMTAG_T (
  ITEMTAGID number NOT NULL,
  ITEMID number NOT NULL,
  TAGID varchar2(36) NOT NULL,
  TAGLABEL varchar2(255) NOT NULL,
  TAGCOLLECTIONID varchar2(36) NOT NULL,
  TAGCOLLECTIONNAME varchar2(255) NOT NULL,
  PRIMARY KEY (ITEMTAGID),
  CONSTRAINT FK_ITEMTAG_ITEM FOREIGN KEY (ITEMID) REFERENCES SAM_ITEM_T (ITEMID)
);

CREATE INDEX SAM_ITEMTAG_ITEMID_I ON SAM_ITEMTAG_T(ITEMID);

CREATE SEQUENCE SAM_ITEMTAG_ID_S;

CREATE TABLE SAM_PUBLISHEDITEMTAG_T (
  ITEMTAGID number NOT NULL,
  ITEMID number NOT NULL,
  TAGID varchar2(36) NOT NULL,
  TAGLABEL varchar2(255) NOT NULL,
  TAGCOLLECTIONID varchar2(36) NOT NULL,
  TAGCOLLECTIONNAME varchar2(255) NOT NULL,
  PRIMARY KEY (ITEMTAGID),
  CONSTRAINT FK_ITEMTAG_ITEM_ITEM FOREIGN KEY (ITEMID) REFERENCES SAM_PUBLISHEDITEM_T (ITEMID)
);

CREATE INDEX SAM_PUBLISHEDITEMTAG_ITEMID_I ON SAM_PUBLISHEDITEMTAG_T(ITEMID);

CREATE SEQUENCE SAM_PITEMTAG_ID_S;


--END SAM-3115

-- SAK-31819 Quartz scheduler

CREATE TABLE context_mapping (
  uuid varchar2(255) NOT NULL,
  componentId varchar2(255) DEFAULT NULL,
  contextId varchar2(255) DEFAULT NULL,
  PRIMARY KEY (uuid),
  CONSTRAINT UK_CONTEXT_MAPPING UNIQUE (componentId,contextId)
);

-- END SAK-31819

-- SAK-SAK-33772 - Add LTI 1.3 Data model items

ALTER TABLE lti_content ADD lti13 NUMBER(2) DEFAULT '0';
ALTER TABLE lti_content ADD lti13_settings CLOB DEFAULT NULL;
ALTER TABLE lti_tools ADD lti13 NUMBER(2) DEFAULT '0';
ALTER TABLE lti_tools ADD lti13_settings CLOB DEFAULT NULL;

-- END SAK-33772

-- SAK-32173 Syllabus remove open in new window option

ALTER TABLE SAKAI_SYLLABUS_ITEM DROP COLUMN openInNewWindow;

-- END SAK-33173 

-- SAK-33896  Remove site manage site association code
DROP TABLE SITEASSOC_CONTEXT_ASSOCIATION;

--END SAK-33896 
commit;
--
-- SAM-3346 and LSNBLDR-924
--
declare
    type ObjNames is table of varchar2(100);
    sequences ObjNames := ObjNames('LESSON_BUILDER_PAGE_S',
        'LESSON_BUILDER_COMMENTS_S',
        'LESSON_BUILDER_GROUPS_S',
        'LESSON_BUILDER_ITEMS_S',
        'LESSON_BUILDER_PROP_S',
        'LESSON_BUILDER_QR_S',
        'LESSON_BUILDER_STPAGE_S',
        'LESSON_BUILDER_LOG_S',
        'LESSON_BUILDER_QRES_S',
        'SAM_FAVORITECOLCHOICES_S','SAM_FAVORITECOLCHOICESITEM_S');
    tablenames ObjNames := ObjNames('lesson_builder_pages',
        'lesson_builder_comments',
        'lesson_builder_groups',
        'lesson_builder_items',
        'lesson_builder_properties',
        'lesson_builder_qr_totals',
        'lesson_builder_student_pages',
        'lesson_builder_log',
        'lesson_builder_q_responses',
        'SAM_FAVORITECOLCHOICES_T','SAM_FAVORITECOLCHOICESITEM_T');
    tablecolumns ObjNames := ObjNames('pageId',
        'id','id','id','id','id','id','id','id',
        'favoriteId','favoriteItemId');
    lnum number(10);
    stc varchar2(1000);
begin
    for i in sequences.first .. sequences.last
    loop
        stc := 'select nvl(max('||tablecolumns(i)||'),0)+1 from '||tablenames(i);
        execute immediate stc into lnum;
        stc := 'create sequence '||sequences(i)||' start with '||lnum;
        --dbms_output.put_line(stc);
        execute immediate stc;
    end loop;
end;

-- KNL-945 Hibernate changes

ALTER TABLE CHAT2_MESSAGE MODIFY CHANNEL_ID varchar2(36);
ALTER TABLE CHAT2_MESSAGE MODIFY MESSAGE_ID varchar2(36);
ALTER TABLE CHAT2_CHANNEL MODIFY CHANNEL_ID varchar2(36);

ALTER TABLE SAKAI_SESSION MODIFY SESSION_SERVER varchar2(255);

-- END KNL-945

-- KNL-1566

-- These are already not null --bbailla2
--ALTER TABLE SAKAI_USER MODIFY MODIFIEDON TIMESTAMP NOT NULL;
--ALTER TABLE SAKAI_USER MODIFY CREATEDON TIMESTAMP NOT NULL;

-- END KNL-1566

-- SAK-40528 Add index on ASN_ASSIGNMENT.CONTEXT
CREATE INDEX IDX_ASN_ASSIGNMENT_CONTEXT ON ASN_ASSIGNMENT(CONTEXT);
-- END SAK-40528

-- sakai_19_oracle_conversion.sql:

-- We did this! --bbailla2
--ALTER TABLE MFR_TOPIC_T ADD ALLOW_EMAIL_NOTIFICATIONS NUMBER(1,0) DEFAULT 1 NOT NULL;
--ALTER TABLE MFR_TOPIC_T ADD INCLUDE_CONTENTS_IN_EMAILS NUMBER(1,0) DEFAULT 1 NOT NULL;

-- END SAK-38427

-- SAK-33969
ALTER TABLE MFR_OPEN_FORUM_T ADD RESTRICT_PERMS_FOR_GROUPS NUMBER(1) DEFAULT 0;
ALTER TABLE MFR_TOPIC_T ADD RESTRICT_PERMS_FOR_GROUPS NUMBER(1) DEFAULT 0;
-- SAK-33969

-- SAK-39967

CREATE INDEX contentreview_provider_id_idx on CONTENTREVIEW_ITEM (providerId, externalId);

-- End SAK-39967

-- SAK-40182
DECLARE
    seq_start INTEGER;
BEGIN
   SELECT NVL(MAX(PUBLISHEDSECTIONMETADATAID),0) + 1
   INTO   seq_start   FROM SAM_PUBLISHEDSECTIONMETADATA_T;
   EXECUTE IMMEDIATE 'CREATE SEQUENCE SAM_PUBSECTIONMETADATA_ID_S START WITH '||seq_start||' INCREMENT BY 1 NOMAXVALUE';
END;

-- End SAK-40182

-- SAK-41021
ALTER TABLE SIGNUP_TS_ATTENDEES ADD INSCRIPTION_TIME TIMESTAMP;

ALTER TABLE SIGNUP_TS_WAITINGLIST ADD INSCRIPTION_TIME TIMESTAMP;

-- END SAK-41021

-- SAK-40967
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'rubrics.evaluee');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'rubrics.evaluator');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'rubrics.associator');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'rubrics.editor');

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.associator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.editor'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.associator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.editor'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.associator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.editor'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));

-- End SAK-40967

-- SAK-40721
ALTER TABLE BULLHORN_ALERTS ADD DEFERRED NUMBER(1) DEFAULT 0 NOT NULL;
-- END SAK-40721

-- SAK-41017

--We did this, but I saw one of these has a '0' in our db --bbailla2
UPDATE SAKAI_SITE_PAGE SET layout = '0' WHERE page_id = '!error-100';
UPDATE SAKAI_SITE_PAGE SET layout = '0' WHERE page_id = '!urlError-100';

-- End of SAK-41017

-- SAK-33855 add settings for display of stats
ALTER TABLE gb_gradebook_t ADD assignment_stats_displayed NUMBER(1,0) DEFAULT 1 NOT NULL;
ALTER TABLE gb_gradebook_t ADD course_grade_stats_displayed NUMBER(1,0) DEFAULT 1 NOT NULL;
-- end SAK-33855

-- SAK-41225
DELETE FROM EMAIL_TEMPLATE_ITEM WHERE template_key = 'polls.notifyDeletedOption' AND template_locale='default';
-- End of SAK-41225

ALTER TABLE lti_tools
--  ADD (allowfa_icon NUMBER(1) DEFAULT 0) We already have this --bbailla2
  ADD (allowlineitems NUMBER(1) DEFAULT 0)
  ADD (rolemap CLOB)
  ADD (lti13_client_id varchar2(1024) DEFAULT NULL)
  ADD (lti13_tool_public CLOB)
  ADD (lti13_tool_keyset CLOB)
  ADD (lti13_tool_kid varchar2(1024) DEFAULT NULL)
  ADD (lti13_tool_private CLOB)
  ADD (lti13_platform_public CLOB)
  ADD (lti13_platform_private CLOB)
  ADD (lti13_oidc_endpoint varchar2(1024) DEFAULT NULL)
  ADD (lti13_oidc_redirect varchar2(1024) DEFAULT NULL)
  ADD (lti11_launch_type NUMBER(1) DEFAULT 0);

ALTER TABLE lti_deploy ADD allowlineitems NUMBER(1) DEFAULT 0;

DELETE from SAKAI_MESSAGE_BUNDLE where PROP_VALUE is NULL;

-- Rubrics
CREATE TABLE rbc_criterion (
  id NUMBER(19) NOT NULL,
  description CLOB NULL,
  created BLOB NULL,
  creatorId VARCHAR2(255) NULL,
  modified BLOB NULL,
  ownerId VARCHAR2(255) NULL,
  ownerType VARCHAR2(255) NULL,
  shared NUMBER(1) NOT NULL,
  title VARCHAR2(255) NULL,
  rubric_id NUMBER(19) DEFAULT NULL NULL,
  CONSTRAINT PK_RBC_CRITERION PRIMARY KEY (id)
);

-- Generate ID using sequence 
CREATE SEQUENCE rbc_crit_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE rbc_criterion_outcome (
  id NUMBER(19) NOT NULL,
  comments VARCHAR2(255) NULL,
  criterion_id NUMBER(19) DEFAULT NULL NULL,
  points NUMBER(10) DEFAULT NULL NULL,
  pointsAdjusted NUMBER(1) NOT NULL,
  selected_rating_id NUMBER(19) DEFAULT NULL NULL,
  CONSTRAINT PK_RBC_CRITERION_OUTCOME PRIMARY KEY (id)
);

-- Generate ID using sequence 
CREATE SEQUENCE rbc_crit_out_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE rbc_criterion_ratings (
  rbc_criterion_id NUMBER(19) NOT NULL,
  ratings_id NUMBER(19) NOT NULL,
  order_index NUMBER(10) NOT NULL,
  CONSTRAINT PK_RBC_CRITERION_RATINGS PRIMARY KEY (rbc_criterion_id, order_index),
  UNIQUE (ratings_id)
);

CREATE TABLE rbc_eval_criterion_outcomes (
  rbc_evaluation_id NUMBER(19) NOT NULL,
  criterionOutcomes_id NUMBER(19) NOT NULL,
  UNIQUE (criterionOutcomes_id)
);

CREATE TABLE rbc_evaluation (
  id NUMBER(19) NOT NULL,
  evaluated_item_id VARCHAR2(255) NULL,
  evaluated_item_owner_id VARCHAR2(255) NULL,
  evaluator_id VARCHAR2(255) NULL,
  created BLOB NULL,
  creatorId VARCHAR2(255) NULL,
  modified BLOB NULL,
  ownerId VARCHAR2(255) NULL,
  ownerType VARCHAR2(255) NULL,
  shared NUMBER(1) NOT NULL,
  overallComment VARCHAR2(255) NULL,
  association_id NUMBER(19) NOT NULL,
  CONSTRAINT PK_RBC_EVALUATION PRIMARY KEY (id)
);

-- Generate ID using sequence 
CREATE SEQUENCE rbc_eval_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE rbc_rating (
  id NUMBER(19) NOT NULL,
  description CLOB NULL,
  created BLOB NULL,
  creatorId VARCHAR2(255) NULL,
  modified BLOB NULL,
  ownerId VARCHAR2(255) NULL,
  ownerType VARCHAR2(255) NULL,
  shared NUMBER(1) NOT NULL,
  points NUMBER(10) DEFAULT NULL NULL,
  title VARCHAR2(255) NULL,
  criterion_id NUMBER(19) DEFAULT NULL NULL,
  CONSTRAINT PK_RBC_RATING PRIMARY KEY (id)
);

-- Generate ID using sequence 
CREATE SEQUENCE rbc_rat_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE rbc_rubric (
  id NUMBER(19) NOT NULL,
  description VARCHAR2(255) NULL,
  created BLOB NULL,
  creatorId VARCHAR2(255) NULL,
  modified BLOB NULL,
  ownerId VARCHAR2(255) NULL,
  ownerType VARCHAR2(255) NULL,
  shared NUMBER(1) NOT NULL,
  title VARCHAR2(255) NULL,
  CONSTRAINT PK_RBC_RUBRIC PRIMARY KEY (id)
);

-- Generate ID using sequence 
CREATE SEQUENCE rbc_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE rbc_rubric_criterions (
  rbc_rubric_id NUMBER(19) NOT NULL,
  criterions_id NUMBER(19) NOT NULL,
  order_index NUMBER(10) NOT NULL,
  CONSTRAINT PK_RBC_RUBRIC_CRITERIONS PRIMARY KEY (rbc_rubric_id, order_index)
);

CREATE TABLE rbc_tool_item_rbc_assoc (
  id NUMBER(19) NOT NULL,
  itemId VARCHAR2(255) NULL,
  created BLOB NULL,
  creatorId VARCHAR2(255) NULL,
  modified BLOB NULL,
  ownerId VARCHAR2(255) NULL,
  ownerType VARCHAR2(255) NULL,
  shared NUMBER(1) NOT NULL,
  rubric_id NUMBER(19) DEFAULT NULL NULL,
  toolId VARCHAR2(255) NULL,
  CONSTRAINT PK_RBC_TOOL_ITEM_RBC_ASSOC PRIMARY KEY (id)
);

-- Generate ID using sequence 
CREATE SEQUENCE rbc_tool_item_rbc_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE rbc_tool_item_rbc_assoc_conf (
  association_id NUMBER(19) NOT NULL,
  parameters NUMBER(1) DEFAULT 0 NULL,
  parameter_label VARCHAR2(255) NOT NULL,
  CONSTRAINT PK_RBC_TOOL_ITEM_RBC_ASSO_CONF PRIMARY KEY (association_id, parameter_label)
);

CREATE INDEX FK_52ca0oi01i6aykocyb9840o37 ON rbc_criterion(rubric_id);
CREATE INDEX FK_6dwej9j9vx5viukv8w86chbbc ON rbc_tool_item_rbc_assoc(rubric_id);
CREATE INDEX FK_cc847hghhh56xmwcaxmevyhrn ON rbc_eval_criterion_outcomes(rbc_evaluation_id);
CREATE INDEX FK_h43853lsee9xsay4qlic80pkv ON rbc_criterion_outcome(criterion_id);
CREATE INDEX FK_n44rjf77gscr2kqkamfbpkc7t ON rbc_rating(criterion_id);
CREATE INDEX FK_soau1ppw2wakbx8hemaaanubi ON rbc_rubric_criterions(criterions_id);

ALTER TABLE rbc_evaluation ADD CONSTRAINT UK_dn0jue890jn9p7vs6tvnsf2gf UNIQUE (association_id, evaluated_item_id, evaluator_id);
ALTER TABLE rbc_criterion ADD CONSTRAINT FK_52ca0oi01i6aykocyb9840o37 FOREIGN KEY (rubric_id) REFERENCES rbc_rubric (id) ;
ALTER TABLE rbc_tool_item_rbc_assoc ADD CONSTRAINT FK_6dwej9j9vx5viukv8w86chbbc FOREIGN KEY (rubric_id) REFERENCES rbc_rubric (id) ;
ALTER TABLE rbc_rubric_criterions ADD CONSTRAINT FK_6jo83t1ddebdbt9296y1xftkn FOREIGN KEY (rbc_rubric_id) REFERENCES rbc_rubric (id) ;
ALTER TABLE rbc_eval_criterion_outcomes ADD CONSTRAINT FK_cc847hghhh56xmwcaxmevyhrn FOREIGN KEY (rbc_evaluation_id) REFERENCES rbc_evaluation (id) ;
ALTER TABLE rbc_eval_criterion_outcomes ADD CONSTRAINT FK_f8xy8709bllewhbve9ias2vk4 FOREIGN KEY (criterionOutcomes_id) REFERENCES rbc_criterion_outcome (id) ;
ALTER TABLE rbc_evaluation ADD CONSTRAINT FK_faohmo8ewmybgp67w10g53dtm FOREIGN KEY (association_id) REFERENCES rbc_tool_item_rbc_assoc (id) ;
ALTER TABLE rbc_criterion_ratings ADD CONSTRAINT FK_funjjd0xkrmm5x300r7i4la83 FOREIGN KEY (ratings_id) REFERENCES rbc_rating (id) ;
ALTER TABLE rbc_criterion_outcome ADD CONSTRAINT FK_h43853lsee9xsay4qlic80pkv FOREIGN KEY (criterion_id) REFERENCES rbc_criterion (id) ;
ALTER TABLE rbc_criterion_ratings ADD CONSTRAINT FK_h4u89cj06chitnt3vcdsu5t7m FOREIGN KEY (rbc_criterion_id) REFERENCES rbc_criterion (id) ;
ALTER TABLE rbc_rating ADD CONSTRAINT FK_n44rjf77gscr2kqkamfbpkc7t FOREIGN KEY (criterion_id) REFERENCES rbc_criterion (id) ;
ALTER TABLE rbc_tool_item_rbc_assoc_conf ADD CONSTRAINT FK_rdpid6jl4csvfv6la80ppu6p9 FOREIGN KEY (association_id) REFERENCES rbc_tool_item_rbc_assoc (id) ;
ALTER TABLE rbc_rubric_criterions ADD CONSTRAINT FK_soau1ppw2wakbx8hemaaanubi FOREIGN KEY (criterions_id) REFERENCES rbc_criterion (id) ;
-- END Rubrics

-- SAK-40687
ALTER TABLE GB_GRADABLE_OBJECT_T ADD EXTERNAL_DATA CLOB;
-- END SAK-40687

-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permission into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('maintain','rubrics.associator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('maintain','rubrics.editor');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('maintain','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('maintain','rubrics.evaluee');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('access','rubrics.evaluee');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Instructor','rubrics.associator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Instructor','rubrics.editor');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Instructor','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Instructor','rubrics.evaluee');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Teaching Assistant','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Teaching Assistant','rubrics.evaluee');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Student','rubrics.evaluee');
--Added for OWL
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','rubrics.associator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','rubrics.editor');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','rubrics.evaluee');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','rubrics.associator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','rubrics.editor');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','rubrics.evaluee');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','rubrics.associator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','rubrics.editor');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','rubrics.evaluee');

-- lookup the role and function number
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
FROM PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new function into the roles of any existing realm that has the role (don't convert the "!site.helper")
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

-- sakai_19_0 -> sakai_19_1 --------------------------------------------------------------

-- SAK-41207 Add indexes
CREATE INDEX UK_hyk73ocki8gwvm3ajf8ls08ac ON ASN_ASSIGNMENT_ATTACHMENTS (ASSIGNMENT_ID);
CREATE INDEX UK_8ewbxsplke3c487h0tjujvtm ON ASN_ASSIGNMENT_GROUPS (ASSIGNMENT_ID);
CREATE INDEX UK_jg017qxc4pv3mdf07c1xpytb8 ON ASN_SUBMISSION_ATTACHMENTS (SUBMISSION_ID);
CREATE INDEX UK_3dou5gsqcya4rwwy99l91fofb ON ASN_SUBMISSION_FEEDBACK_ATTACH (SUBMISSION_ID);
-- END SAK-41207

-- SAK-41828 remove grade override from submitter when not a group submission
UPDATE ASN_SUBMISSION_SUBMITTER ss
    SET ss.GRADE = NULL
    WHERE EXISTS
        ( SELECT 1 FROM ASN_SUBMISSION_SUBMITTER ss1
            JOIN ASN_SUBMISSION s ON (s.SUBMISSION_ID = ss.SUBMISSION_ID)
            JOIN ASN_ASSIGNMENT a ON (s.ASSIGNMENT_ID = a.ASSIGNMENT_ID)
            WHERE a.IS_GROUP = 0
                AND s.grade IS NOT NULL
                AND ss1.grade IS NOT NULL
        );

-- END SAK-41828

-- /sakai_19_0 -> sakai_19_1 -------------------------------------------------------------

-- sakai_19_1 -> sakai_19_2 -------------------------------------------------------------

-- SAK-41878
CREATE INDEX UK_f0kvf8pq0xapndnamvw5xr2ib ON ASN_SUBMISSION_SUBMITTER (SUBMITTER);
-- SAK-41878

-- SAK-41841
ALTER TABLE RBC_CRITERION_OUTCOME ADD COMMENTS_COPY VARCHAR2(255);
UPDATE RBC_CRITERION_OUTCOME SET COMMENTS_COPY = COMMENTS;
UPDATE RBC_CRITERION_OUTCOME SET COMMENTS = NULL;
ALTER TABLE RBC_CRITERION_OUTCOME MODIFY COMMENTS LONG;
ALTER TABLE RBC_CRITERION_OUTCOME MODIFY COMMENTS CLOB;
UPDATE RBC_CRITERION_OUTCOME SET COMMENTS = COMMENTS_COPY;
ALTER TABLE RBC_CRITERION_OUTCOME DROP COLUMN COMMENTS_COPY;
-- SAK-41841-- SAK-38427

-- /sakai_19_1 -> sakai_19_2 -------------------------------------------------------------

-- SAK_41228
-- We did this; doesn't hurt: --bbailla2
UPDATE CM_MEMBERSHIP_T SET USER_ID = LOWER(USER_ID);
UPDATE CM_ENROLLMENT_T SET USER_ID = LOWER(USER_ID);
UPDATE CM_OFFICIAL_INSTRUCTORS_T SET INSTRUCTOR_ID = LOWER(INSTRUCTOR_ID);
-- End of SAK_41228

-- SAK-41391

--We contrib'd this --bbailla2
--ALTER TABLE POLL_OPTION ADD OPTION_ORDER NUMBER(10,0);

-- END SAK-41391

-- SAK-41825
-- Removed "column" keyword, that's invalid in Oracle (should contribute this) --bbailla2
ALTER TABLE SAM_ASSESSMENTBASE_T ADD CATEGORYID NUMBER(19);
ALTER TABLE SAM_PUBLISHEDASSESSMENT_T ADD CATEGORYID NUMBER(19);
-- END SAK-41825

-- User Activity

-- We did this! --bbailla2
/*CREATE TABLE SST_DETAILED_EVENTS
   (ID NUMBER(19,0) NOT NULL,
	USER_ID VARCHAR2(99 CHAR) NOT NULL,
	SITE_ID VARCHAR2(99 CHAR) NOT NULL,
	EVENT_ID VARCHAR2(32 CHAR) NOT NULL,
	EVENT_DATE TIMESTAMP (6) NOT NULL,
	EVENT_REF VARCHAR2(512 CHAR) NOT NULL,
	 PRIMARY KEY (ID));

create index IDX_DE_SITE_ID_DATE on SST_DETAILED_EVENTS(SITE_ID,EVENT_DATE);
create index IDX_DE_SITE_ID_USER_ID_DATE on SST_DETAILED_EVENTS(SITE_ID,USER_ID,EVENT_DATE);

create sequence SST_DETAILED_EVENTS_ID;

INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'sitestats.usertracking.track');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (SAKAI_REALM_FUNCTION_SEQ.NEXTVAL, 'sitestats.usertracking.be.tracked');

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'sitestats.usertracking.track'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'sitestats.usertracking.be.tracked'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'sitestats.usertracking.track'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'sitestats.usertracking.be.tracked'));

CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('maintain','sitestats.usertracking.track');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('access','sitestats.usertracking.be.tracked');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Instructor','sitestats.usertracking.track');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Student','sitestats.usertracking.be.tracked');

CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
FROM PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY)
SELECT
    SRRFD.REALM_KEY, SRRFD.ROLE_KEY, TMP.FUNCTION_KEY
FROM
    (SELECT DISTINCT SRRF.REALM_KEY, SRRF.ROLE_KEY FROM SAKAI_REALM_RL_FN SRRF) SRRFD
    JOIN PERMISSIONS_TEMP TMP ON (SRRFD.ROLE_KEY = TMP.ROLE_KEY)
    JOIN SAKAI_REALM SR ON (SRRFD.REALM_KEY = SR.REALM_KEY)
    WHERE SR.REALM_ID != '!site.helper' AND SR.REALM_ID NOT LIKE '!user.template%'
    AND NOT EXISTS (
        SELECT 1
            FROM SAKAI_REALM_RL_FN SRRFI
            WHERE SRRFI.REALM_KEY=SRRFD.REALM_KEY AND SRRFI.ROLE_KEY=SRRFD.ROLE_KEY AND SRRFI.FUNCTION_KEY=TMP.FUNCTION_KEY
    );

DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;*/

-- End User Activity

-- SAK-34741
ALTER TABLE SAM_ITEM_T ADD ISEXTRACREDIT NUMBER(1);
ALTER TABLE SAM_PUBLISHEDITEM_T ADD ISEXTRACREDIT NUMBER(1);
-- END SAK-34741

-- START SAK-42700
ALTER TABLE rbc_evaluation MODIFY (
   ownerType varchar2(99),
   evaluated_item_owner_id varchar2(99),
   evaluator_id varchar2(99),
   creatorId varchar2(99),
   ownerId varchar2(99)
);

CREATE INDEX rbc_eval_owner ON rbc_evaluation(ownerId);

ALTER TABLE rbc_tool_item_rbc_assoc MODIFY (
   ownerType varchar2(99),
   toolId varchar2(99),
   creatorId varchar2(99),
   ownerId varchar2(99)
);

CREATE INDEX rbc_tool_item_owner ON rbc_tool_item_rbc_assoc(toolId, itemId, ownerId);

ALTER TABLE rbc_criterion MODIFY (
   ownerType varchar2(99),
   creatorId varchar2(99),
   ownerId varchar2(99)
);

ALTER TABLE rbc_rating MODIFY (
   ownerType varchar2(99),
   creatorId varchar2(99),
   ownerId varchar2(99)
);

ALTER TABLE rbc_rubric MODIFY (
   ownerType varchar2(99),
   creatorId varchar2(99),
   ownerId varchar2(99)
);
-- End SAK-42700

-- START SAK-42400
ALTER TABLE SAM_ASSESSACCESSCONTROL_T ADD FEEDBACKENDDATE TIMESTAMP (6);
ALTER TABLE SAM_PUBLISHEDACCESSCONTROL_T ADD FEEDBACKENDDATE TIMESTAMP (6);
ALTER TABLE SAM_ASSESSACCESSCONTROL_T ADD FEEDBACKSCORETHRESHOLD FLOAT(126);
ALTER TABLE SAM_PUBLISHEDACCESSCONTROL_T ADD FEEDBACKSCORETHRESHOLD FLOAT(126);
-- END SAK-42400

---------------------------------------- CONTRIB -----------------------------------------

ALTER TABLE SCORM_CONTENT_PACKAGE_T ADD (SHOW_TOC NUMBER(1,0) DEFAULT 0 NOT NULL);
ALTER TABLE SCORM_CONTENT_PACKAGE_T ADD (SHOW_NAV_BAR NUMBER(1,0) DEFAULT 0 NOT NULL);

----------------------------------- OWL SPECIFIC STUFF -----------------------------------

DELETE FROM QRTZ_TRIGGERS WHERE JOB_NAME IN ('TII Content Review Reports', 'TII Content Review Queue');
DELETE FROM QRTZ_JOB_DETAILS WHERE JOB_NAME IN ('TII Content Review Queue', 'TII Content Review Reports');
-- Temporary (applied on dev):
--DELETE FROM QRTZ_JOB_DETAILS WHERE JOB_NAME IN ('Anon-grading ID sync', 'Poll Option Order Backfill');
