-- ----------------------------------------------------
-- OWL 11.3-owl5.1 -> 20.x-owl1 MySQL conversion script
-- ----------------------------------------------------

-- 11.3 -> 11.4 ----------------------------------------

--
-- SAK-31840 update defaults as its now managed in the POJO
--
alter table GB_GRADABLE_OBJECT_T modify column IS_EXTRA_CREDIT bit(1) DEFAULT NULL;
alter table GB_GRADABLE_OBJECT_T modify column HIDE_IN_ALL_GRADES_TABLE bit(1) DEFAULT NULL;

-- 11.4 -> 11.5 ----------------------------------------

-- SAM-3012 Update samigo events
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

-- END SAM-3012 Update samigo events

-- 11.5 -> 12.x ----------------------------------------

-- After running these conversions and starting your system you must run the Assignment Conversion Job or you will not have assignments.
-- Please see https://github.com/sakaiproject/sakai/blob/12.x/assignment/impl/src/java/org/sakaiproject/assignment/impl/conversion/README.md
-- For additional information!

-- SAM-3016
ALTER TABLE SAM_EVENTLOG_T ADD IPADDRESS varchar(99);

-- SAK-30207
-- CREATE TABLE IF NOT EXISTS CONTENTREVIEW_ITEM (
--     ID                  BIGINT NOT NULL AUTO_INCREMENT,
--     VERSION             INT NOT NULL,
--     PROVIDERID          INT NOT NULL,
--     CONTENTID           VARCHAR(255) NOT NULL,
--     USERID              VARCHAR(255),
--     SITEID              VARCHAR(255),
--     TASKID              VARCHAR(255),
--     EXTERNALID          VARCHAR(255),
--     DATEQUEUED          DATETIME NOT NULL,
--     DATESUBMITTED       DATETIME,
--     DATEREPORTRECEIVED  DATETIME,
--     STATUS              BIGINT,
--     REVIEWSCORE         INT,
--     LASTERROR           LONGTEXT,
--     RETRYCOUNT          BIGINT,
--     NEXTRETRYTIME       DATETIME NOT NULL,
--     ERRORCODE           INT,
--     PRIMARY KEY (ID),
--     CONSTRAINT PROVIDERID UNIQUE (PROVIDERID, CONTENTID)
-- );
-- END SAK-30207

-- ^ OWL NOTE: we must instead alter our table to add version and providerId (default -1 since everything existing will stop working),
-- and we must eventually drop the LTI columns urlaccessed, submissionid, resubmission, and externalgrade
-- for now, let them be null
ALTER TABLE CONTENTREVIEW_ITEM ADD VERSION INT DEFAULT 0 NOT NULL;
ALTER TABLE CONTENTREVIEW_ITEM ADD PROVIDERID INT DEFAULT -1 NOT NULL;
alter table CONTENTREVIEW_ITEM modify column URLACCESSED bit;
alter table CONTENTREVIEW_ITEM modify column EXTERNALGRADE varchar(255);

-- SAK-33723 Content review item properties
CREATE TABLE CONTENTREVIEW_ITEM_PROPERTIES (
  CONTENTREVIEW_ITEM_ID bigint(20) NOT NULL,
  VALUE varchar(255) DEFAULT NULL,
  PROPERTY varchar(255) NOT NULL,
  PRIMARY KEY (CONTENTREVIEW_ITEM_ID,PROPERTY),
  CONSTRAINT FOREIGN KEY (CONTENTREVIEW_ITEM_ID) REFERENCES CONTENTREVIEW_ITEM (id)
);

-- CONTENTREVIEW_ITEM.PROVIDERID
-- Possible Provider Ids
-- Compilatio = 1372282923
-- Turnitin = 199481773
-- VeriCite = 1930781763
-- Urkund = 1752904483

-- *** IMPORTANT ***
-- If you have used CONTENT REVIEW previously then you may need to run the following:
-- ALTER TABLE CONTENTREVIEW_ITEM ADD COLUMN PROVIDERID INT NOT NULL;
-- If you have used multiple content review implementations then you will need to update the correct providerid with the matching content review items
-- Example where only Turnitin was configured:
-- UPDATE CONTENTREVIEW_ITEM SET PROVIDERID = 199481773;

-- END SAK-33723

--
-- SAK-31641 Switch from INTs to VARCHARs in Oauth
--
ALTER TABLE OAUTH_ACCESSORS
CHANGE
  status status VARCHAR(255),
  CHANGE type type VARCHAR(255)
;

UPDATE OAUTH_ACCESSORS SET status = CASE
  WHEN status = 0 THEN "VALID"
  WHEN status = 1 THEN "REVOKED"
  WHEN status = 2 THEN "EXPIRED"
END;

UPDATE OAUTH_ACCESSORS SET type = CASE
  WHEN type = 0 THEN "REQUEST"
  WHEN type = 1 THEN "REQUEST_AUTHORISING"
  WHEN type = 2 THEN "REQUEST_AUTHORISED"
  WHEN type = 3 THEN "ACCESS"
END;

--
-- SAK-31636 Rename existing 'Home' tools
--

update SAKAI_SITE_PAGE set title = 'Overview' where title = 'Home';

--
-- SAK-31563
--

-- Add new user_id columns and their corresponding indexes
ALTER TABLE pasystem_popup_assign ADD user_id varchar(99);
ALTER TABLE pasystem_popup_dismissed ADD user_id varchar(99);
ALTER TABLE pasystem_banner_dismissed ADD user_id varchar(99);

CREATE INDEX popup_assign_lower_user_id on pasystem_popup_assign (user_id);
CREATE INDEX popup_dismissed_lower_user_id on pasystem_popup_dismissed (user_id);
CREATE INDEX banner_dismissed_user_id on pasystem_banner_dismissed (user_id);

-- Map existing EIDs to their corresponding user IDs
update pasystem_popup_assign popup set user_id = (select user_id from SAKAI_USER_ID_MAP map where popup.user_eid = map.eid);
update pasystem_popup_dismissed popup set user_id = (select user_id from SAKAI_USER_ID_MAP map where popup.user_eid = map.eid);
update pasystem_banner_dismissed banner set user_id = (select user_id from SAKAI_USER_ID_MAP map where banner.user_eid = map.eid);

-- Any rows that couldn't be mapped are dropped (there shouldn't
-- really be any, but if there are those users were already being
-- ignored when identified by EID)
DELETE FROM pasystem_popup_assign WHERE user_id is null;
DELETE FROM pasystem_popup_dismissed WHERE user_id is null;
DELETE FROM pasystem_banner_dismissed WHERE user_id is null;

-- Enforce NULL checks on the new columns
ALTER TABLE pasystem_popup_assign MODIFY user_id varchar(99) NOT NULL;
ALTER TABLE pasystem_popup_dismissed MODIFY user_id varchar(99) NOT NULL;
ALTER TABLE pasystem_banner_dismissed MODIFY user_id varchar(99) NOT NULL;

-- Reintroduce unique constraints for the new column
ALTER TABLE pasystem_popup_dismissed drop INDEX unique_popup_dismissed;
ALTER TABLE pasystem_popup_dismissed add UNIQUE INDEX unique_popup_dismissed (user_id, state, uuid);

ALTER TABLE pasystem_banner_dismissed drop INDEX unique_banner_dismissed;
ALTER TABLE pasystem_banner_dismissed add UNIQUE INDEX unique_banner_dismissed (user_id, state, uuid);

-- Drop the old columns
ALTER TABLE pasystem_popup_assign DROP COLUMN user_eid;
ALTER TABLE pasystem_popup_dismissed DROP COLUMN user_eid;
ALTER TABLE pasystem_banner_dismissed DROP COLUMN user_eid;

-- LSNBLDR-633 Restrict editing of Lessons pages and subpages to one person
ALTER TABLE lesson_builder_pages ADD owned bit default false not null;
-- END LSNBLDR-633

-- BEGIN SAK-31819 Remove the old ScheduledInvocationManager job as it's not present in Sakai 12.
DELETE FROM QRTZ_SIMPLE_TRIGGERS WHERE TRIGGER_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
DELETE FROM QRTZ_TRIGGERS WHERE TRIGGER_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
-- This one is the actual job that the triggers were trying to run
DELETE FROM QRTZ_JOB_DETAILS WHERE JOB_NAME='org.sakaiproject.component.app.scheduler.ScheduledInvocationManagerImpl.runner';
-- END SAK-31819

-- BEGIN SAK-15708 avoid duplicate rows
-- Brian B's note: Shouldn't be necessary, but in case we missed something and we end up with a duplicate between now and outs20. The index stuff below is still needed.
CREATE TABLE SAKAI_POSTEM_STUDENT_DUPES (
  id bigint(20) NOT NULL,
  username varchar(99),
  surrogate_key bigint(20)
);
INSERT INTO SAKAI_POSTEM_STUDENT_DUPES SELECT MAX(id), username, surrogate_key FROM SAKAI_POSTEM_STUDENT GROUP BY username, surrogate_key HAVING count(id) > 1;
DELETE FROM SAKAI_POSTEM_STUDENT_GRADES WHERE student_id IN (SELECT id FROM SAKAI_POSTEM_STUDENT_DUPES);
DELETE FROM SAKAI_POSTEM_STUDENT WHERE id IN (SELECT id FROM SAKAI_POSTEM_STUDENT_DUPES);
DROP TABLE SAKAI_POSTEM_STUDENT_DUPES;

ALTER TABLE SAKAI_POSTEM_STUDENT MODIFY COLUMN username varchar(99), DROP INDEX POSTEM_STUDENT_USERNAME_I,
  ADD UNIQUE INDEX POSTEM_USERNAME_SURROGATE (username, surrogate_key);
-- END SAK-15708

-- BEGIN SAK-32083 TAGS

CREATE TABLE IF NOT EXISTS `tagservice_collection` (
  `tagcollectionid` CHAR(36) PRIMARY KEY,
  `description` TEXT,
  `externalsourcename` VARCHAR(255) UNIQUE,
  `externalsourcedescription` TEXT,
  `name` VARCHAR(255) UNIQUE,
  `createdby` VARCHAR(255),
  `creationdate` BIGINT,
  `lastmodifiedby` VARCHAR(255),
  `lastmodificationdate` BIGINT,
  `lastsynchronizationdate` BIGINT,
  `externalupdate` BOOLEAN,
  `externalcreation` BOOLEAN,
  `lastupdatedateinexternalsystem` BIGINT
);

CREATE TABLE IF NOT EXISTS `tagservice_tag` (
  `tagid` CHAR(36) PRIMARY KEY,
  `tagcollectionid` CHAR(36) NOT NULL,
  `externalid` VARCHAR(255),
  `taglabel` VARCHAR(255),
  `description` TEXT,
  `alternativelabels` TEXT,
  `createdby` VARCHAR(255),
  `creationdate` BIGINT,
  `externalcreation` BOOLEAN,
  `externalcreationDate` BIGINT,
  `externalupdate` BOOLEAN,
  `lastmodifiedby` VARCHAR(255),
  `lastmodificationdate` BIGINT,
  `lastupdatedateinexternalsystem` BIGINT,
  `parentid` VARCHAR(255),
  `externalhierarchycode` TEXT,
  `externaltype` VARCHAR(255),
  `data` TEXT,
  INDEX tagservice_tag_taglabel (taglabel),
  INDEX tagservice_tag_tagcollectionid (tagcollectionid),
  INDEX tagservice_tag_externalid (externalid),
  FOREIGN KEY (tagcollectionid)
  REFERENCES tagservice_collection(tagcollectionid)
    ON DELETE RESTRICT
);

INSERT IGNORE INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('tagservice.manage');
-- END SAK-32083 TAGS

-- KNL-1566
ALTER TABLE SAKAI_USER CHANGE MODIFIEDON MODIFIEDON DATETIME NOT NULL;
ALTER TABLE SAKAI_USER CHANGE CREATEDON CREATEDON DATETIME NOT NULL;

-- OWLNOTE: At this point in the Sakai master script there is a new grade point scale, but we are not using it
-- so it is not included here

-- SAM-1129 Change the column DESCRIPTION of SAM_QUESTIONPOOL_T from VARCHAR(255) to longtext
ALTER TABLE SAM_QUESTIONPOOL_T MODIFY DESCRIPTION longtext;

-- SAK-30461 Portal bullhorns
CREATE TABLE BULLHORN_ALERTS
(
    ID bigint NOT NULL AUTO_INCREMENT,
    ALERT_TYPE varchar(8) NOT NULL,
    FROM_USER varchar(99) NOT NULL,
    TO_USER varchar(99) NOT NULL,
    EVENT varchar(32) NOT NULL,
    REF varchar(255) NOT NULL,
    TITLE varchar(255),
    SITE_ID varchar(99),
    URL TEXT NOT NULL,
    EVENT_DATE datetime NOT NULL,
    PRIMARY KEY(ID)
);

-- SAK-32417 Forums permission composite index
ALTER TABLE MFR_PERMISSION_LEVEL_T ADD INDEX MFR_COMPOSITE_PERM (TYPE_UUID, NAME);

-- SAK-32442 - LTI Column cleanup
-- These conversions may fail if you started Sakai at newer versions that didn't contain these columns/tables
set @exist_Check := (
    select count(*) from information_schema.columns
    where TABLE_NAME='lti_tools'
    and COLUMN_NAME='enabled_capability'
    and TABLE_SCHEMA=database()
) ;
set @sqlstmt := if(@exist_Check>0,'alter table lti_tools drop column enabled_capability', 'select ''''') ;
prepare stmt from @sqlstmt ;
execute stmt;

set @exist_Check := (
    select count(*) from information_schema.columns
    where TABLE_NAME='lti_tools'
    and COLUMN_NAME='allowlori'
    and TABLE_SCHEMA=database()
) ;
set @sqlstmt := if(@exist_Check>0,'alter table lti_tools drop column allowlori', 'select ''''') ;
prepare stmt from @sqlstmt ;
execute stmt;

set @exist_Check := (
    select count(*) from information_schema.columns
    where TABLE_NAME='lti_deploy'
    and COLUMN_NAME='allowlori'
    and TABLE_SCHEMA=database()
) ;
set @sqlstmt := if(@exist_Check>0,'alter table lti_deploy drop column allowlori', 'select ''''') ;
prepare stmt from @sqlstmt ;
execute stmt;

drop table IF EXISTS lti_mapping;
-- END SAK-32442

-- SAK-32572  SAK-33910 Additional permission settings for Messages

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.myGroupRoles');

-- The permission above is false for all users by default
-- if you want to turn this feature on for all "student/access" type roles, then run
-- the following conversion:

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Content Designer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Librarian'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Auditor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupRoles'));


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

-- END SAK-32572 Additional permission settings for Messages

-- SAK-33430 user_audits_log is queried against site_id
ALTER TABLE user_audits_log
  MODIFY COLUMN site_id varchar(99),
  MODIFY COLUMN role_name varchar(99),
  DROP INDEX user_audits_log_index,
  ADD INDEX user_audits_log_index(site_id);
-- END SAK-33430

-- SAK-33406 - Allow reorder of LTI plugin tools
ALTER TABLE lti_tools ADD toolorder int(11) DEFAULT '0';
ALTER TABLE lti_content ADD toolorder int(11) DEFAULT '0';
-- END SAK-33406

-- SAK-33898
ALTER TABLE lti_content ADD sha256 TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD sha256 TINYINT DEFAULT '0';
-- END SAK-33898

-- -- BEGIN SAK-32045 -- Update My Workspace to My Home
-- UPDATE SAKAI_SITE
-- SET TITLE = 'Home', DESCRIPTION = 'Home'
-- WHERE SITE_ID LIKE '!user%';
--
-- UPDATE SAKAI_SITE
-- SET TITLE = 'Home', DESCRIPTION = 'Home'
-- WHERE TITLE = 'My Workspace'
-- AND SITE_ID LIKE '~%';
--
-- UPDATE SAKAI_SITE_TOOL
-- SET TITLE = 'Home'
-- WHERE REGISTRATION = 'sakai.iframe.myworkspace';
-- -- END SAK-32045

-- ^ OWLNOTE: I think we already did this so commenting it out

-- SAK-SAK-33772 - Add LTI 1.3 Data model items

ALTER TABLE lti_content ADD     lti13 TINYINT DEFAULT '0';
ALTER TABLE lti_content ADD     lti13_settings MEDIUMTEXT;
ALTER TABLE lti_tools ADD     lti13 TINYINT DEFAULT '0';
ALTER TABLE lti_tools ADD     lti13_settings MEDIUMTEXT;

-- END SAK-33772

-- SAK-32440 - Add LTI site info config

ALTER TABLE lti_tools ADD     siteinfoconfig tinyint(4) DEFAULT '0';

-- END SAK-32440

-- SAK-32642 Commons Tools

CREATE TABLE COMMONS_COMMENT (
  ID char(36) NOT NULL,
  POST_ID char(36) DEFAULT NULL,
  CONTENT mediumtext NOT NULL,
  CREATOR_ID varchar(99) NOT NULL,
  CREATED_DATE datetime NOT NULL,
  MODIFIED_DATE datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY creator_id (CREATOR_ID),
  KEY post_id (POST_ID)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE COMMONS_COMMONS_POST (
  COMMONS_ID char(36) DEFAULT NULL,
  POST_ID char(36) DEFAULT NULL,
  UNIQUE KEY commons_id_post_id (COMMONS_ID,POST_ID)
);

CREATE TABLE COMMONS_COMMONS (
  ID char(36) NOT NULL,
  SITE_ID varchar(99) NOT NULL,
  EMBEDDER varchar(24) NOT NULL,
  PRIMARY KEY (ID)
);

CREATE TABLE COMMONS_POST (
  ID char(36) NOT NULL,
  CONTENT mediumtext NOT NULL,
  CREATOR_ID varchar(99) NOT NULL,
  CREATED_DATE datetime NOT NULL,
  MODIFIED_DATE datetime NOT NULL,
  RELEASE_DATE datetime NOT NULL,
  PRIMARY KEY (ID),
  KEY creator_id (CREATOR_ID)
);

-- END SAK-32642

-- SAM-2970 Extended Time

CREATE TABLE SAM_EXTENDEDTIME_T (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  ASSESSMENT_ID bigint(20) DEFAULT NULL,
  PUB_ASSESSMENT_ID bigint(20) DEFAULT NULL,
  USER_ID varchar(255) DEFAULT NULL,
  GROUP_ID varchar(255) DEFAULT NULL,
  START_DATE datetime DEFAULT NULL,
  DUE_DATE datetime DEFAULT NULL,
  RETRACT_DATE datetime DEFAULT NULL,
  TIME_HOURS int(11) DEFAULT NULL,
  TIME_MINUTES int(11) DEFAULT NULL,
  PRIMARY KEY (ID),
  KEY (ASSESSMENT_ID),
  KEY (PUB_ASSESSMENT_ID),
  CONSTRAINT FOREIGN KEY (PUB_ASSESSMENT_ID) REFERENCES SAM_PUBLISHEDASSESSMENT_T (ID),
  CONSTRAINT FOREIGN KEY (ASSESSMENT_ID) REFERENCES SAM_ASSESSMENTBASE_T (ID)
);

-- END SAM-2970

-- SAK-31819 Quartz scheduler

CREATE TABLE context_mapping (
  uuid varchar(255) NOT NULL,
  componentId varchar(255) DEFAULT NULL,
  contextId varchar(255) DEFAULT NULL,
  PRIMARY KEY (uuid),
  UNIQUE KEY (componentId,contextId)
);

-- END SAK-31819

-- SAM-3115 Tags and Search in Samigo

ALTER TABLE SAM_ITEM_T ADD COLUMN HASH varchar(255) DEFAULT NULL;
ALTER TABLE SAM_PUBLISHEDITEM_T ADD COLUMN HASH varchar(255) DEFAULT NULL;
ALTER TABLE SAM_PUBLISHEDITEM_T ADD COLUMN ITEMHASH varchar(255) DEFAULT NULL;

CREATE TABLE SAM_ITEMTAG_T (
  ITEMTAGID bigint(20) NOT NULL AUTO_INCREMENT,
  ITEMID bigint(20) NOT NULL,
  TAGID varchar(36) NOT NULL,
  TAGLABEL varchar(255) NOT NULL,
  TAGCOLLECTIONID varchar(36) NOT NULL,
  TAGCOLLECTIONNAME varchar(255) NOT NULL,
  PRIMARY KEY (ITEMTAGID),
  KEY SAM_ITEMTAG_ITEMID_I (ITEMID),
  CONSTRAINT FOREIGN KEY (ITEMID) REFERENCES SAM_ITEM_T (ITEMID)
);

CREATE TABLE SAM_PUBLISHEDITEMTAG_T (
  ITEMTAGID bigint(20) NOT NULL AUTO_INCREMENT,
  ITEMID bigint(20) NOT NULL,
  TAGID varchar(36) NOT NULL,
  TAGLABEL varchar(255) NOT NULL,
  TAGCOLLECTIONID varchar(36) NOT NULL,
  TAGCOLLECTIONNAME varchar(255) NOT NULL,
  PRIMARY KEY (ITEMTAGID),
  KEY SAM_PUBLISHEDITEMTAG_ITEMID_I (ITEMID),
  CONSTRAINT FOREIGN KEY (ITEMID) REFERENCES SAM_PUBLISHEDITEM_T (ITEMID)
);

-- END SAM-3115

-- SAK-32173 Syllabus remove open in new window option

ALTER TABLE SAKAI_SYLLABUS_ITEM DROP COLUMN openInNewWindow;

-- END SAK-32173

-- SAK-33896 Remove this site association table
drop table IF EXISTS SITEASSOC_CONTEXT_ASSOCIATION;
-- END SAK-33896

-- SAK-32101 - Assignment service refactor
CREATE TABLE ASN_ASSIGNMENT (
    ASSIGNMENT_ID                  VARCHAR(36) NOT NULL,
    ALLOW_ATTACHMENTS              BIT,
    ALLOW_PEER_ASSESSMENT          BIT,
    AUTHOR                         VARCHAR(99),
    CLOSE_DATE                     DATETIME,
    CONTENT_REVIEW                 BIT,
    CONTEXT                        VARCHAR(99) NOT NULL,
    CREATED_DATE                   DATETIME NOT NULL,
    MODIFIED_DATE                  DATETIME,
    DELETED                        BIT,
    DRAFT                          BIT NOT NULL,
    DROP_DEAD_DATE                 DATETIME,
    DUE_DATE                       DATETIME,
    HIDE_DUE_DATE                  BIT,
    HONOR_PLEDGE                   BIT,
    INDIVIDUALLY_GRADED            BIT,
    INSTRUCTIONS                   LONGTEXT,
    IS_GROUP                       BIT,
    MAX_GRADE_POINT                INT,
    MODIFIER                       VARCHAR(99),
    OPEN_DATE                      DATETIME,
    PEER_ASSESSMENT_ANON_EVAL      BIT,
    PEER_ASSESSMENT_INSTRUCTIONS   LONGTEXT,
    PEER_ASSESSMENT_NUMBER_REVIEW  INT,
    PEER_ASSESSMENT_PERIOD_DATE    DATETIME,
    PEER_ASSESSMENT_STUDENT_REVIEW BIT,
    POSITION                       INT,
    RELEASE_GRADES                 BIT,
    SCALE_FACTOR                   INT,
    SECTION                        VARCHAR(255),
    TITLE                          VARCHAR(255),
    ACCESS_TYPE                    VARCHAR(255) NOT NULL,
    GRADE_TYPE                     INT,
    SUBMISSION_TYPE                INT,
    VISIBLE_DATE                   DATETIME,
    PRIMARY KEY(ASSIGNMENT_ID)
);

CREATE TABLE ASN_ASSIGNMENT_ATTACHMENTS (
    ASSIGNMENT_ID VARCHAR(36) NOT NULL,
    ATTACHMENT    VARCHAR(1024),
    CONSTRAINT FK_HYK73OCKI8GWVM3AJF8LS08AC FOREIGN KEY(ASSIGNMENT_ID) REFERENCES ASN_ASSIGNMENT(ASSIGNMENT_ID),
    INDEX FK_HYK73OCKI8GWVM3AJF8LS08AC(ASSIGNMENT_ID)
);

CREATE TABLE ASN_ASSIGNMENT_GROUPS (
    ASSIGNMENT_ID VARCHAR(36) NOT NULL,
    GROUP_ID      VARCHAR(255),
    CONSTRAINT FK_8EWBXSPLKE3C487H0TJUJVTM FOREIGN KEY(ASSIGNMENT_ID) REFERENCES ASN_ASSIGNMENT(ASSIGNMENT_ID),
    INDEX FK_8EWBXSPLKE3C487H0TJUJVTM(ASSIGNMENT_ID)
);

CREATE TABLE ASN_ASSIGNMENT_PROPERTIES (
    ASSIGNMENT_ID VARCHAR(36) NOT NULL,
    VALUE         LONGTEXT,
    NAME          VARCHAR(255) NOT NULL,
    PRIMARY KEY(ASSIGNMENT_ID,NAME),
    CONSTRAINT FK_GDAT1B6UQIUI9MXDKTD6M5IG1 FOREIGN KEY(ASSIGNMENT_ID) REFERENCES ASN_ASSIGNMENT(ASSIGNMENT_ID)
);

CREATE TABLE ASN_SUBMISSION (
    SUBMISSION_ID    VARCHAR(36) NOT NULL,
    CREATED_DATE     DATETIME,
    MODIFIED_DATE    DATETIME,
    RETURNED_DATE    DATETIME,
    SUBMITTED_DATE   DATETIME,
    FACTOR           INT,
    FEEDBACK_COMMENT LONGTEXT,
    FEEDBACK_TEXT    LONGTEXT,
    GRADE            VARCHAR(32),
    GRADE_RELEASED   BIT,
    GRADED           BIT,
    GRADED_BY        VARCHAR(99),
    GROUP_ID         VARCHAR(36),
    HIDDEN_DUE_DATE  BIT,
    HONOR_PLEDGE     BIT,
    RETURNED         BIT,
    SUBMITTED        BIT,
    TEXT             LONGTEXT,
    USER_SUBMISSION  BIT,
    PRIVATE_NOTES    LONGTEXT,
    ASSIGNMENT_ID    VARCHAR(36),
    PRIMARY KEY(SUBMISSION_ID),
    CONSTRAINT FK_6A25A0BXIFPYEIJ72PDK7XRLR FOREIGN KEY(ASSIGNMENT_ID) REFERENCES ASN_ASSIGNMENT(ASSIGNMENT_ID),
    INDEX FK_6A25A0BXIFPYEIJ72PDK7XRLR(ASSIGNMENT_ID)
);

CREATE TABLE ASN_SUBMISSION_ATTACHMENTS (
    SUBMISSION_ID VARCHAR(36) NOT NULL,
    ATTACHMENT    VARCHAR(1024),
    CONSTRAINT FK_JG017QXC4PV3MDF07C1XPYTB8 FOREIGN KEY(SUBMISSION_ID) REFERENCES ASN_SUBMISSION(SUBMISSION_ID),
    INDEX FK_JG017QXC4PV3MDF07C1XPYTB8(SUBMISSION_ID)
);

CREATE TABLE ASN_SUBMISSION_FEEDBACK_ATTACH (
    SUBMISSION_ID       VARCHAR(36) NOT NULL,
    FEEDBACK_ATTACHMENT VARCHAR(1024),
    CONSTRAINT FK_3DOU5GSQCYA4RWWY99L91FOFB FOREIGN KEY(SUBMISSION_ID) REFERENCES ASN_SUBMISSION(SUBMISSION_ID),
    INDEX FK_3DOU5GSQCYA4RWWY99L91FOFB(SUBMISSION_ID)
);

CREATE TABLE ASN_SUBMISSION_PROPERTIES (
    SUBMISSION_ID VARCHAR(36) NOT NULL,
    VALUE         LONGTEXT,
    NAME          VARCHAR(255) NOT NULL,
    PRIMARY KEY(SUBMISSION_ID,NAME),
    CONSTRAINT FK_2K0JAT40WAP5EKWKPSN201EAU FOREIGN KEY(SUBMISSION_ID) REFERENCES ASN_SUBMISSION(SUBMISSION_ID)
);

CREATE TABLE ASN_SUBMISSION_SUBMITTER (
    ID            BIGINT NOT NULL AUTO_INCREMENT,
    FEEDBACK      LONGTEXT,
    GRADE         VARCHAR(32),
    SUBMITTEE     BIT NOT NULL,
    SUBMITTER     VARCHAR(99) NOT NULL,
    SUBMISSION_ID VARCHAR(36) NOT NULL,
    PRIMARY KEY(ID),
    CONSTRAINT FK_TKKCY78P5G4XRYKRIUIMOJWV5 FOREIGN KEY(SUBMISSION_ID) REFERENCES ASN_SUBMISSION(SUBMISSION_ID),
    CONSTRAINT UK_FHL15YNESBCTBUS4859J78D8F UNIQUE(SUBMISSION_ID,SUBMITTER)
);

ALTER TABLE ASN_PEER_ASSESSMENT_ITEM_T MODIFY REVIEW_COMMENT longtext;

-- END SAK-32101

-- KNL-945 Hibernate changes
ALTER TABLE CHAT2_MESSAGE
    DROP FOREIGN KEY FK720F9882555E0B79,
    MODIFY CHANNEL_ID varchar(36),
    MODIFY MESSAGE_ID varchar(36);
ALTER TABLE CHAT2_CHANNEL MODIFY CHANNEL_ID varchar(36);
ALTER TABLE CHAT2_MESSAGE ADD CONSTRAINT FK720F9882555E0B79 FOREIGN KEY (CHANNEL_ID) REFERENCES CHAT2_CHANNEL(CHANNEL_ID);

ALTER TABLE SAKAI_SESSION MODIFY SESSION_SERVER varchar(255);

-- END KNL-945

-- KNL-1484 Reduce column lengths to support mysql utf8mb4 databases

ALTER TABLE SAKAI_CONFIG_ITEM MODIFY DESCRIPTION varchar(3000);
ALTER TABLE SAKAI_CONFIG_ITEM MODIFY DEFAULT_VALUE varchar(3000);
ALTER TABLE SAKAI_CONFIG_ITEM MODIFY RAW_VALUE varchar(3000);
ALTER TABLE SAKAI_CONFIG_ITEM MODIFY VALUE varchar(3000);

-- END KNL-1484

-- SAK-40528 Add index on ASN_ASSIGNMENT.CONTEXT
ALTER TABLE ASN_ASSIGNMENT ADD INDEX `IDX_ASN_ASSIGNMENT_CONTEXT` (`CONTEXT`);
-- END SAK-40528

-- 12.x -> 19.0 ----------------------------------------

-- SAK-38427
ALTER TABLE MFR_TOPIC_T ADD COLUMN ALLOW_EMAIL_NOTIFICATIONS BIT(1) NOT NULL DEFAULT 1;
ALTER TABLE MFR_TOPIC_T ADD COLUMN INCLUDE_CONTENTS_IN_EMAILS BIT(1) NOT NULL DEFAULT 1;
-- END SAK-38427

-- SAK-33969
ALTER TABLE MFR_OPEN_FORUM_T ADD RESTRICT_PERMS_FOR_GROUPS BIT(1) DEFAULT FALSE;
ALTER TABLE MFR_TOPIC_T ADD RESTRICT_PERMS_FOR_GROUPS BIT(1) DEFAULT FALSE;
-- END SAK-33969

-- SAK-41021
ALTER TABLE SIGNUP_TS_ATTENDEES ADD INSCRIPTION_TIME datetime;
ALTER TABLE SIGNUP_TS_WAITINGLIST ADD INSCRIPTION_TIME datetime;
-- END SAK-41021

-- SAK-40967
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rubrics.evaluee');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rubrics.evaluator');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rubrics.associator');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'rubrics.editor');

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
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.associator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.editor'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.associator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.editor'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.associator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.editor'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluator'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rubrics.evaluee'));
-- END SAK-40967

-- SAK-40721
ALTER TABLE BULLHORN_ALERTS ADD COLUMN DEFERRED BIT(1) NOT NULL DEFAULT b'0';
-- END SAK-40721

-- SAK-41017
UPDATE SAKAI_SITE_PAGE SET layout = '0' WHERE page_id = '!error-100';
UPDATE SAKAI_SITE_PAGE SET layout = '0' WHERE page_id = '!urlError-100';
-- END SAK-41017

-- SAK-33855 add settings for display of stats
ALTER TABLE GB_GRADEBOOK_T ADD COLUMN ASSIGNMENT_STATS_DISPLAYED bit(1) NOT NULL DEFAULT b'1';
ALTER TABLE GB_GRADEBOOK_T ADD COLUMN COURSE_GRADE_STATS_DISPLAYED bit(1) NOT NULL DEFAULT b'1';
-- end SAK-33855

-- SAK-41225
DELETE FROM EMAIL_TEMPLATE_ITEM WHERE template_key = 'polls.notifyDeletedOption' AND template_locale='default';
-- End of SAK-41225

ALTER TABLE lti_tools ADD allowlineitems TINYINT(3) DEFAULT 0 NULL;
ALTER TABLE lti_tools ADD allowfa_icon TINYINT(3) DEFAULT 0 NULL;
ALTER TABLE lti_tools ADD rolemap MEDIUMTEXT NULL;
ALTER TABLE lti_tools ADD lti13_client_id VARCHAR(1024) NULL;
ALTER TABLE lti_tools ADD lti13_tool_public MEDIUMTEXT NULL;
ALTER TABLE lti_tools ADD lti13_tool_keyset MEDIUMTEXT NULL;
ALTER TABLE lti_tools ADD lti13_tool_kid VARCHAR(1024) NULL;
ALTER TABLE lti_tools ADD lti13_tool_private MEDIUMTEXT NULL;
ALTER TABLE lti_tools ADD lti13_platform_public MEDIUMTEXT NULL;
ALTER TABLE lti_tools ADD lti13_platform_private MEDIUMTEXT NULL;
ALTER TABLE lti_tools ADD lti13_oidc_endpoint VARCHAR(1024) NULL;
ALTER TABLE lti_tools ADD lti13_oidc_redirect VARCHAR(1024) NULL;
ALTER TABLE lti_tools ADD lti11_launch_type TINYINT(3) DEFAULT 0 NULL;

ALTER TABLE lti_deploy ADD COLUMN allowlineitems TINYINT DEFAULT 0;

DELETE SAKAI_MESSAGE_BUNDLE from SAKAI_MESSAGE_BUNDLE where PROP_VALUE is NULL;

-- SAK-40687
ALTER TABLE GB_GRADABLE_OBJECT_T ADD EXTERNAL_DATA LONGTEXT NULL;
-- END SAK-40687

-- Rubrics
CREATE TABLE rbc_criterion (
  id BIGINT AUTO_INCREMENT NOT NULL,
  `description` LONGTEXT NULL,
  created TINYBLOB DEFAULT NULL NULL,
  creatorId VARCHAR(255) NULL,
  modified TINYBLOB DEFAULT NULL NULL,
  ownerId VARCHAR(255) NULL,
  ownerType VARCHAR(255) NULL,
  shared BIT(1) NOT NULL,
  title VARCHAR(255) NULL,
  rubric_id BIGINT DEFAULT NULL NULL,
  CONSTRAINT PK_RBC_CRITERION PRIMARY KEY (id)
);

CREATE TABLE rbc_criterion_outcome (
  id BIGINT AUTO_INCREMENT NOT NULL,
  comments VARCHAR(255) NULL,
  criterion_id BIGINT DEFAULT NULL NULL,
  points INT DEFAULT NULL NULL,
  pointsAdjusted BIT(1) NOT NULL,
  selected_rating_id BIGINT DEFAULT NULL NULL,
  CONSTRAINT PK_RBC_CRITERION_OUTCOME PRIMARY KEY (id)
);

CREATE TABLE rbc_criterion_ratings (
  rbc_criterion_id BIGINT NOT NULL,
  ratings_id BIGINT NOT NULL,
  order_index INT NOT NULL,
  CONSTRAINT PK_RBC_CRITERION_RATINGS PRIMARY KEY (rbc_criterion_id, order_index),
  UNIQUE (ratings_id)
);

CREATE TABLE rbc_eval_criterion_outcomes (
  rbc_evaluation_id BIGINT NOT NULL,
  criterionOutcomes_id BIGINT NOT NULL,
  UNIQUE (criterionOutcomes_id)
);

CREATE TABLE rbc_evaluation (
  id BIGINT AUTO_INCREMENT NOT NULL,
  evaluated_item_id VARCHAR(255) NULL,
  evaluated_item_owner_id VARCHAR(255) NULL,
  evaluator_id VARCHAR(255) NULL,
  created TINYBLOB DEFAULT NULL NULL,
  creatorId VARCHAR(255) NULL,
  modified TINYBLOB DEFAULT NULL NULL,
  ownerId VARCHAR(255) NULL,
  ownerType VARCHAR(255) NULL,
  shared BIT(1) NOT NULL,
  overallComment VARCHAR(255) NULL,
  association_id BIGINT NOT NULL,
  CONSTRAINT PK_RBC_EVALUATION PRIMARY KEY (id)
);

CREATE TABLE rbc_rating (
  id BIGINT AUTO_INCREMENT NOT NULL,
  `description` LONGTEXT NULL,
  created TINYBLOB DEFAULT NULL NULL,
  creatorId VARCHAR(255) NULL,
  modified TINYBLOB DEFAULT NULL NULL,
  ownerId VARCHAR(255) NULL,
  ownerType VARCHAR(255) NULL,
  shared BIT(1) NOT NULL,
  points INT DEFAULT NULL NULL,
  title VARCHAR(255) NULL,
  criterion_id BIGINT DEFAULT NULL NULL,
  CONSTRAINT PK_RBC_RATING PRIMARY KEY (id)
);

CREATE TABLE rbc_rubric (
  id BIGINT AUTO_INCREMENT NOT NULL,
  `description` VARCHAR(255) NULL,
  created TINYBLOB DEFAULT NULL NULL,
  creatorId VARCHAR(255) NULL,
  modified TINYBLOB DEFAULT NULL NULL,
  ownerId VARCHAR(255) NULL,
  ownerType VARCHAR(255) NULL,
  shared BIT(1) NOT NULL,
  title VARCHAR(255) NULL,
  CONSTRAINT PK_RBC_RUBRIC PRIMARY KEY (id)
);

CREATE TABLE rbc_rubric_criterions (
  rbc_rubric_id BIGINT NOT NULL,
  criterions_id BIGINT NOT NULL,
  order_index INT NOT NULL,
  CONSTRAINT PK_RBC_RUBRIC_CRITERIONS PRIMARY KEY (rbc_rubric_id, order_index)
);

CREATE TABLE rbc_tool_item_rbc_assoc (
  id BIGINT AUTO_INCREMENT NOT NULL,
  itemId VARCHAR(255) NULL,
  created TINYBLOB DEFAULT NULL NULL,
  creatorId VARCHAR(255) NULL,
  modified TINYBLOB DEFAULT NULL NULL,
  ownerId VARCHAR(255) NULL,
  ownerType VARCHAR(255) NULL,
  shared BIT(1) NOT NULL,
  rubric_id BIGINT DEFAULT NULL NULL,
  toolId VARCHAR(255) NULL,
  CONSTRAINT PK_RBC_TOOL_ITEM_RBC_ASSOC PRIMARY KEY (id)
);

CREATE TABLE rbc_tool_item_rbc_assoc_conf (
  association_id BIGINT NOT NULL,
  parameters BIT(1) DEFAULT 0 NULL,
  parameter_label VARCHAR(255) NOT NULL,
  CONSTRAINT PK_RBC_TOOL_ITEM_RBC_ASSOC_CONF PRIMARY KEY (association_id, parameter_label)
);

CREATE INDEX FK_52ca0oi01i6aykocyb9840o37 ON rbc_criterion(rubric_id);
CREATE INDEX FK_6dwej9j9vx5viukv8w86chbbc ON rbc_tool_item_rbc_assoc(rubric_id);
CREATE INDEX FK_cc847hghhh56xmwcaxmevyhrn ON rbc_eval_criterion_outcomes(rbc_evaluation_id);
CREATE INDEX FK_h43853lsee9xsay4qlic80pkv ON rbc_criterion_outcome(criterion_id);
CREATE INDEX FK_n44rjf77gscr2kqkamfbpkc7t ON rbc_rating(criterion_id);
CREATE INDEX FK_soau1ppw2wakbx8hemaaanubi ON rbc_rubric_criterions(criterions_id);

ALTER TABLE rbc_evaluation ADD CONSTRAINT UK_dn0jue890jn9p7vs6tvnsf2gf UNIQUE (association_id, evaluated_item_id, evaluator_id);
ALTER TABLE rbc_criterion ADD CONSTRAINT FK_52ca0oi01i6aykocyb9840o37 FOREIGN KEY (rubric_id) REFERENCES rbc_rubric (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_tool_item_rbc_assoc ADD CONSTRAINT FK_6dwej9j9vx5viukv8w86chbbc FOREIGN KEY (rubric_id) REFERENCES rbc_rubric (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_rubric_criterions ADD CONSTRAINT FK_6jo83t1ddebdbt9296y1xftkn FOREIGN KEY (rbc_rubric_id) REFERENCES rbc_rubric (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_eval_criterion_outcomes ADD CONSTRAINT FK_cc847hghhh56xmwcaxmevyhrn FOREIGN KEY (rbc_evaluation_id) REFERENCES rbc_evaluation (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_eval_criterion_outcomes ADD CONSTRAINT FK_f8xy8709bllewhbve9ias2vk4 FOREIGN KEY (criterionOutcomes_id) REFERENCES rbc_criterion_outcome (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_evaluation ADD CONSTRAINT FK_faohmo8ewmybgp67w10g53dtm FOREIGN KEY (association_id) REFERENCES rbc_tool_item_rbc_assoc (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_criterion_ratings ADD CONSTRAINT FK_funjjd0xkrmm5x300r7i4la83 FOREIGN KEY (ratings_id) REFERENCES rbc_rating (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_criterion_outcome ADD CONSTRAINT FK_h43853lsee9xsay4qlic80pkv FOREIGN KEY (criterion_id) REFERENCES rbc_criterion (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_criterion_ratings ADD CONSTRAINT FK_h4u89cj06chitnt3vcdsu5t7m FOREIGN KEY (rbc_criterion_id) REFERENCES rbc_criterion (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_rating ADD CONSTRAINT FK_n44rjf77gscr2kqkamfbpkc7t FOREIGN KEY (criterion_id) REFERENCES rbc_criterion (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_tool_item_rbc_assoc_conf ADD CONSTRAINT FK_rdpid6jl4csvfv6la80ppu6p9 FOREIGN KEY (association_id) REFERENCES rbc_tool_item_rbc_assoc (id) ON UPDATE RESTRICT ON DELETE RESTRICT;
ALTER TABLE rbc_rubric_criterions ADD CONSTRAINT FK_soau1ppw2wakbx8hemaaanubi FOREIGN KEY (criterions_id) REFERENCES rbc_criterion (id) ON UPDATE RESTRICT ON DELETE RESTRICT;

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
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','rubrics.associator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','rubrics.editor');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Secondary Instructor','rubrics.evaluee');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','rubrics.associator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','rubrics.editor');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Grade Admin','rubrics.evaluee');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','rubrics.associator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','rubrics.editor');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','rubrics.evaluator');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES ('Course Coordinator','rubrics.evaluee');

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

-- END Rubrics

-- 19.0 -> 19.1 ----------------------------------------

-- SAK-41207 Add indexes
CREATE INDEX UK_hyk73ocki8gwvm3ajf8ls08ac ON ASN_ASSIGNMENT_ATTACHMENTS (ASSIGNMENT_ID);
CREATE INDEX UK_8ewbxsplke3c487h0tjujvtm ON ASN_ASSIGNMENT_GROUPS (ASSIGNMENT_ID);
CREATE INDEX UK_jg017qxc4pv3mdf07c1xpytb8 ON ASN_SUBMISSION_ATTACHMENTS (SUBMISSION_ID);
CREATE INDEX UK_3dou5gsqcya4rwwy99l91fofb ON ASN_SUBMISSION_FEEDBACK_ATTACH (SUBMISSION_ID);

DROP INDEX FK_hyk73ocki8gwvm3ajf8ls08ac ON ASN_ASSIGNMENT_ATTACHMENTS;
DROP INDEX FK_8ewbxsplke3c487h0tjujvtm ON ASN_ASSIGNMENT_GROUPS;
DROP INDEX FK_jg017qxc4pv3mdf07c1xpytb8 ON ASN_SUBMISSION_ATTACHMENTS;
DROP INDEX FK_3dou5gsqcya4rwwy99l91fofb ON ASN_SUBMISSION_FEEDBACK_ATTACH;
-- END SAK-41207

-- SAK-41828 remove grade override from submitter when not a group submission
UPDATE ASN_SUBMISSION_SUBMITTER ss
        JOIN ASN_SUBMISSION s ON (s.SUBMISSION_ID = ss.SUBMISSION_ID)
        JOIN ASN_ASSIGNMENT a ON (s.ASSIGNMENT_ID = a.ASSIGNMENT_ID)
        SET ss.GRADE = NULL
        WHERE a.IS_GROUP IS FALSE AND s.grade IS NOT NULL AND ss.grade IS NOT NULL;
-- END SAK-41828

-- 19.1 -> 19.2 ----------------------------------------

-- SAK-41878
CREATE INDEX UK_f0kvf8pq0xapndnamvw5xr2ib ON ASN_SUBMISSION_SUBMITTER (SUBMITTER);
-- SAK-41878

-- SAK-41841
ALTER TABLE RBC_CRITERION_OUTCOME MODIFY COMMENTS LONGTEXT;
-- SAK-41841

-- 19.2 -> 19.3 ----------------------------------------

-- SAK-42409
CREATE INDEX IDX_BULLHORN_ALERTS_ALERT_TYPE_TO_USER ON BULLHORN_ALERTS(ALERT_TYPE,TO_USER);
-- END SAK-41017

-- 19.3 -> 20.0 ----------------------------------------

-- SAK_41228
UPDATE CM_MEMBERSHIP_T SET USER_ID = LOWER(USER_ID);
UPDATE CM_ENROLLMENT_T SET USER_ID = LOWER(USER_ID);
UPDATE CM_OFFICIAL_INSTRUCTORS_T SET INSTRUCTOR_ID = LOWER(INSTRUCTOR_ID);
-- End of SAK_41228

-- SAK-41391

ALTER TABLE POLL_OPTION ADD OPTION_ORDER INTEGER;

-- END SAK-41391

-- SAK-41825
ALTER TABLE SAM_ASSESSMENTBASE_T ADD CATEGORYID BIGINT(20);
ALTER TABLE SAM_PUBLISHEDASSESSMENT_T ADD CATEGORYID BIGINT(20);
-- END SAK-41825

-- SAK-34741
-- ALTER TABLE SAM_ITEM_T ADD ISEXTRACREDIT TINYINT(1);
-- ALTER TABLE SAM_PUBLISHEDITEM_T ADD ISEXTRACREDIT TINYINT(1);
-- END SAK-34741

-- SCO-155
ALTER TABLE `SCORM_CONTENT_PACKAGE_T` ADD `SHOW_TOC` BIT NOT NULL DEFAULT 0;
-- END SCO-155

-- SCO-167
ALTER TABLE `SCORM_CONTENT_PACKAGE_T` ADD `SHOW_NAV_BAR` BIT NOT NULL DEFAULT 0;
-- END SCO-167

-- START SAK-42400
ALTER TABLE SAM_ASSESSACCESSCONTROL_T ADD FEEDBACKENDDATE DATETIME;
ALTER TABLE SAM_PUBLISHEDACCESSCONTROL_T ADD FEEDBACKENDDATE DATETIME;
ALTER TABLE SAM_ASSESSACCESSCONTROL_T ADD FEEDBACKSCORETHRESHOLD DOUBLE;
ALTER TABLE SAM_PUBLISHEDACCESSCONTROL_T ADD FEEDBACKSCORETHRESHOLD DOUBLE;
-- END SAK-42400

-- SAK-41172
CREATE TABLE SAKAI_REALM_LOCKS (
 REALM_KEY INTEGER NOT NULL,
 REFERENCE VARCHAR (255) NOT NULL,
 LOCK_MODE INTEGER NOT NULL
);

ALTER TABLE SAKAI_REALM_LOCKS
 ADD ( PRIMARY KEY (REALM_KEY, REFERENCE) ) ;

ALTER TABLE SAKAI_REALM_LOCKS
 ADD ( FOREIGN KEY (REALM_KEY)
 REFERENCES SAKAI_REALM (REALM_KEY) ) ;
-- END SAK-41172

-- START SAKORA-13
ALTER TABLE SAKORA_MEMBERSHIP ADD COLUMN INPUT_INT integer NULL;
UPDATE SAKORA_MEMBERSHIP SET INPUT_INT=UNIX_TIMESTAMP(INPUT_TIME);
ALTER TABLE SAKORA_MEMBERSHIP DROP COLUMN INPUT_TIME, CHANGE COLUMN INPUT_INT INPUT_TIME integer UNSIGNED NULL DEFAULT NULL;

ALTER TABLE SAKORA_PERSON ADD COLUMN INPUT_INT integer NULL;
UPDATE SAKORA_PERSON SET INPUT_INT=UNIX_TIMESTAMP(INPUT_TIME);
ALTER TABLE SAKORA_PERSON DROP COLUMN INPUT_TIME, CHANGE COLUMN INPUT_INT INPUT_TIME integer UNSIGNED NULL DEFAULT NULL;

ALTER TABLE SAKORA_SESSION ADD COLUMN INPUT_INT integer NULL;
UPDATE SAKORA_SESSION SET INPUT_INT=UNIX_TIMESTAMP(INPUT_TIME);
ALTER TABLE SAKORA_SESSION DROP COLUMN INPUT_TIME, CHANGE COLUMN INPUT_INT INPUT_TIME integer UNSIGNED NULL DEFAULT NULL;
-- END SAKORA-13

-- SAK-42498
ALTER TABLE BULLHORN_ALERTS DROP COLUMN ALERT_TYPE;
-- END SAK-42498

-- OWL-3739/CC-9
ALTER TABLE cert_field_mapping RENAME TO certificate_field_mapping;

UPDATE certificate_criterion
SET type = 'org.sakaiproject.certification.api.criteria.gradebook.GreaterThanScoreCriterion'
WHERE type = 'com.rsmart.certification.impl.hibernate.criteria.gradebook.GreaterThanScoreCriterionHibernateImpl';

UPDATE certificate_criterion
SET type = 'org.sakaiproject.certification.api.criteria.gradebook.WillExpireCriterion'
WHERE type = 'com.rsmart.certification.impl.hibernate.criteria.gradebook.WillExpireCriterionHibernateImpl';

UPDATE certificate_criterion
SET type = 'org.sakaiproject.certification.api.criteria.gradebook.FinalGradeScoreCriterion'
WHERE type = 'com.rsmart.certification.impl.hibernate.criteria.gradebook.FinalGradeScoreCriterionHibernateImpl';

UPDATE certificate_criterion
SET type = 'org.sakaiproject.certification.api.criteria.gradebook.DueDatePassedCriterion'
WHERE type = 'com.rsmart.certification.impl.hibernate.criteria.gradebook.DueDatePassedCriterionHibernateImpl';

UPDATE sakai_site_tool
SET registration = 'sakai.certification'
WHERE registration = 'com.rsmart.certification';
-- END OWL-3739/CC-9

-- OWL-3757
update gb_category_t set is_equal_weight_assns = false where is_equal_weight_assns is null;
alter table gb_category_t modify IS_EQUAL_WEIGHT_ASSNS bit not null default false;

-- OWL-800
UPDATE sam_assessaccesscontrol_t SET latehandling = 2, autosubmit = 1 WHERE assessmentid IN (1, 2, 3, 4, 5, 6, 7);
-- END OWL-800

-- OWLNOTE: User Activity (SAK-40018) is part of the Sakai master script but it is the same as OWL's existing table and permissions

-- SAK-42700 add indexes via JPA for common queries

ALTER TABLE rbc_evaluation
  MODIFY COLUMN ownerType varchar(99),
  MODIFY COLUMN evaluated_item_owner_id varchar(99),
  MODIFY COLUMN evaluator_id varchar(99),
  MODIFY COLUMN creatorId varchar(99),
  MODIFY COLUMN ownerId varchar(99),
  ADD INDEX rbc_eval_owner(ownerId);

ALTER TABLE rbc_tool_item_rbc_assoc
  MODIFY COLUMN ownerType varchar(99),
  MODIFY COLUMN toolId varchar(99),
  MODIFY COLUMN creatorId varchar(99),
  MODIFY COLUMN ownerId varchar(99),
  ADD INDEX rbc_tool_item_owner(toolId, itemId, ownerId);

ALTER TABLE rbc_criterion
  MODIFY COLUMN ownerType varchar(99),
  MODIFY COLUMN creatorId varchar(99),
  MODIFY COLUMN ownerId varchar(99);

ALTER TABLE rbc_rating
  MODIFY COLUMN ownerType varchar(99),
  MODIFY COLUMN creatorId varchar(99),
  MODIFY COLUMN ownerId varchar(99);

ALTER TABLE rbc_rubric
  MODIFY COLUMN ownerType varchar(99),
  MODIFY COLUMN creatorId varchar(99),
  MODIFY COLUMN ownerId varchar(99);

-- END SAK-42700

-- SAK-41175
ALTER TABLE rbc_criterion_outcome MODIFY COLUMN points DOUBLE NULL DEFAULT NULL;
-- END SAK-41175
ALTER TABLE rbc_rating MODIFY COLUMN points DOUBLE NULL DEFAULT NULL;

-- Gradbook Classic tool removed in 19, move tool reference to Gradebook NG
UPDATE SAKAI_SITE_TOOL set REGISTRATION='sakai.gradebookng' where REGISTRATION='sakai.gradebook.tool';

-- SAK-43077
update GB_CATEGORY_T set IS_EQUAL_WEIGHT_ASSNS = false where IS_EQUAL_WEIGHT_ASSNS is null;
alter table GB_CATEGORY_T modify IS_EQUAL_WEIGHT_ASSNS bit not null default false;
-- END SAK-43077

-- BEGIN SAK-42748
ALTER TABLE BULLHORN_ALERTS ADD INDEX IDX_BULLHORN_ALERTS_EVENT_REF(EVENT, REF);
-- END SAK-42748

-- BEGIN SAK-42498
-- likely doesn't exist --bbailla2:
ALTER TABLE BULLHORN_ALERTS DROP INDEX IDX_BULLHORN_ALERTS_ALERT_TYPE_TO_USER;
ALTER TABLE BULLHORN_ALERTS ADD INDEX IDX_BULLHORN_ALERTS_TO_USER(TO_USER);
 -- END SAK-42498

-- SAK-42190 ONEDRIVE
CREATE TABLE ONEDRIVE_USER (
  oneDriveUserId varchar(255) NOT NULL,
  oneDriveName varchar(255) DEFAULT NULL,
  refreshToken longtext,
  sakaiUserId varchar(99) DEFAULT NULL,
  token longtext,
  PRIMARY KEY (oneDriveUserId)
);
-- END SAK-42190 ONEDRIVE

-- SAK-42423 GOOGLEDRIVE
CREATE TABLE GOOGLEDRIVE_USER (
  sakaiUserId varchar(99) NOT NULL,
  googleDriveName varchar(255) DEFAULT NULL,
  refreshToken longtext,
  googleDriveUserId varchar(255) DEFAULT NULL,
  token longtext,
  PRIMARY KEY (sakaiUserId),
  UNIQUE (googleDriveUserId)
);
-- END SAK-42423 GOOGLEDRIVE

-- OWL-2994 - update viewExtraUserProperties to viewStudentNumbers
update sakai_realm_function set function_name = 'gradebook.viewStudentNumbers' where function_name = 'gradebook.viewExtraUserProperties';
-- END OWL-2994

-- SAK-34741
ALTER TABLE SAM_ITEM_T add ISEXTRACREDIT bit(1) not null default 0;
ALTER TABLE SAM_PUBLISHEDITEM_T add ISEXTRACREDIT bit(1) not null default 0;
-- END SAK-34741

-- SAK-41172
DROP FUNCTION IF EXISTS SPLITASSIGNMENTREFERENCES;
DROP PROCEDURE IF EXISTS BUILDGROUPLOCKTABLE;
DELIMITER $$
CREATE FUNCTION SPLITASSIGNMENTREFERENCES(ASSIGNMENTREFERENCES VARCHAR(400), POS INTEGER) RETURNS VARCHAR(400)
BEGIN
    DECLARE OUTPUT VARCHAR(400);
    DECLARE DELIM VARCHAR(3);
    SET DELIM = '#:#';
    SET OUTPUT = REPLACE(
        SUBSTRING(SUBSTRING_INDEX(ASSIGNMENTREFERENCES, DELIM, POS), LENGTH(SUBSTRING_INDEX(ASSIGNMENTREFERENCES, DELIM, POS - 1)) + 1)
        , DELIM
        , '');
    IF OUTPUT = '' THEN SET OUTPUT = NULL; END IF;
    RETURN OUTPUT;
END $$

CREATE PROCEDURE BUILDGROUPLOCKTABLE()
BEGIN
    DECLARE I INTEGER;
    SET I = 1;
    REPEAT
        INSERT INTO SAKAI_REALM_LOCKS (REALM_KEY, REFERENCE, LOCK_MODE)
            SELECT (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = (SELECT CONCAT_WS('/', '/site', SITE_ID, 'group', GROUP_ID))),
                   CONCAT('/assignment/a/', SITE_ID, '/', SUBSTRING_INDEX(SPLITASSIGNMENTREFERENCES(VALUE, I), '/', -1)),
                   1
            FROM SAKAI_SITE_GROUP_PROPERTY
            WHERE SPLITASSIGNMENTREFERENCES(VALUE, I) IS NOT NULL AND NAME='group_prop_locked_by';
        SET I = I + 1;
    UNTIL ROW_COUNT() = 0
        END REPEAT;
END $$

DELIMITER ;

CALL BUILDGROUPLOCKTABLE();
DROP FUNCTION SPLITASSIGNMENTREFERENCES;
DROP PROCEDURE BUILDGROUPLOCKTABLE;
-- END SAK-41172

ALTER TABLE GB_GRADE_RECORD_T DROP COLUMN EXCLUDED;

-- START SAK-41812
ALTER TABLE SAKAI_PERSON_T ADD COLUMN PHONETIC_PRONUNCIATION varchar(255) DEFAULT NULL;
-- END SAK-41812

-- START OWL-3717

-- Create functions
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES('dropbox.write.own');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES('dropbox.write.any');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES('dropbox.delete.own');
INSERT INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES('dropbox.delete.any');

-- Project sites - maintainers get .any permissions, accessors get .own permissions
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'maintain'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'maintain'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'access'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.own')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'access'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.own')
);
-- Give instructor, course coordinator, grade admin, and secondary instructor the '.any' permissions in !site.template.course
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Instructor'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Instructor'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Course Coordinator'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Course Coordinator'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Grade Admin'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Grade Admin'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Secondary Instructor'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.any')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Secondary Instructor'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.any')
);
-- Give student, TA, and auditor the '.own' permissions in !site.template.course
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Student'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.own')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Student'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.own')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Teaching Assistant'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.own')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Teaching Assistant'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.own')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Auditor'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.write.own')
);
INSERT INTO SAKAI_REALM_RL_FN (REALM_KEY, ROLE_KEY, FUNCTION_KEY) VALUES (
    (SELECT REALM_KEY FROM SAKAI_REALM WHERE REALM_ID = '!site.template.course'),
    (SELECT ROLE_KEY FROM SAKAI_REALM_ROLE WHERE ROLE_NAME = 'Auditor'),
    (SELECT FUNCTION_KEY FROM SAKAI_REALM_FUNCTION WHERE FUNCTION_NAME = 'dropbox.delete.own')
);

-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permission into existing realms
-- --------------------------------------------------------------------------------------------------------------------------------------
-- Same process as above
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

INSERT INTO PERMISSIONS_SRC_TEMP VALUES('maintain','dropbox.write.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('maintain','dropbox.delete.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('access','dropbox.write.own');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('access','dropbox.delete.own');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Instructor','dropbox.write.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Instructor','dropbox.delete.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Course Coordinator','dropbox.write.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Course Coordinator','dropbox.delete.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Grade Admin','dropbox.write.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Grade Admin','dropbox.delete.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Secondary Instructor','dropbox.write.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Secondary Instructor','dropbox.delete.any');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Student','dropbox.write.own');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Student','dropbox.delete.own');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Teaching Assistant','dropbox.write.own');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Teaching Assistant','dropbox.delete.own');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Auditor','dropbox.write.own');
INSERT INTO PERMISSIONS_SRC_TEMP VALUES('Auditor','dropbox.delete.own');

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

-- END OWL-3717

-- START SAK-43441
CREATE TEMPORARY TABLE messages_with_rubric AS
    SELECT CONCAT(m.CREATED_BY, ".", m.UUID) evaluated_item_id, m.CREATED_BY evaluee, re.association_id messageAssociationId, m.GRADEASSIGNMENTNAME gbItemId
        FROM MFR_MESSAGE_T m
        INNER JOIN rbc_evaluation re ON re.evaluated_item_id = CONCAT(m.CREATED_BY, ".", m.UUID)
        WHERE m.GRADEASSIGNMENTNAME IS NOT NULL;

UPDATE IGNORE rbc_evaluation re
    INNER JOIN messages_with_rubric mwr ON re.evaluated_item_id = mwr.evaluated_item_id
    INNER JOIN rbc_tool_item_rbc_assoc ra ON mwr.gbItemId = ra.itemId
        SET association_id = ra.id
        ,re.evaluated_item_id = CONCAT(mwr.gbItemId, ".", mwr.evaluee)
        WHERE association_id = messageAssociationId;

DROP TABLE messages_with_rubric;
-- END SAK-43441


-- BEGIN 20.1 -> 20.2

-- SAK-44420
UPDATE poll_option SET deleted = 0 WHERE DELETED IS NULL;
ALTER TABLE poll_option MODIFY COLUMN DELETED BIT NOT NULL DEFAULT 0;
-- End SAK-44420

-- SAK-43497
alter table ASN_ASSIGNMENT_PROPERTIES modify VALUE varchar(4000) null;
-- END SAK-43407

-- SAK-44636 - Add LTI Lessons Placement checkbox - By default it is off for future tool installations
ALTER TABLE lti_tools ADD pl_lessonsselection TINYINT DEFAULT 0;
-- Existing records needed to be switched on
UPDATE lti_tools SET pl_lessonsselection = 1;
-- END SAK-44636

-- END 20.1 -> 20.2
