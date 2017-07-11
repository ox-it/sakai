-- OWL 10.3-owl6 -> 11.3-owl1 MySQL conversion script
-- ---------------------------------------------------

-- 10.3 -> 10.5 ---------------------------------------------------------

-- SAM-2259 Optimizing Samigo question pool performance 
ALTER TABLE SAM_QUESTIONPOOLITEM_T CHANGE COLUMN ITEMID ITEMID bigint(20);
ALTER TABLE SAM_QUESTIONPOOLITEM_T ADD INDEX SAM_IDX_ITEMID (ITEMID);
ALTER TABLE SAM_AUTHZDATA_T DROP INDEX sam_authz_functionId_idx;
ALTER TABLE SAM_AUTHZDATA_T DROP INDEX sam_authz_qualifierId_idx;
ALTER TABLE SAM_AUTHZDATA_T ADD INDEX SAM_IDX_FUNC_QUAL (FUNCTIONID,QUALIFIERID);
ALTER TABLE SAM_AUTHZDATA_T MODIFY COLUMN AGENTID varchar(99);
ALTER TABLE SAM_AUTHZDATA_T MODIFY COLUMN LASTMODIFIEDBY varchar(99);
-- End SAM-2259

-- 10.5 -> 10.6 ----------------------------------------------------------

-- SAK-29571 MFR_MESSAGE_DELETD_I causes bad performance
drop index MFR_MESSAGE_DELETED_I on MFR_MESSAGE_T;
-- END SAK-29571 MFR_MESSAGE_DELETD_I causes bad performance

-- 10.6 -> 11.0 -----------------------------------------------------------

-- SAK-25784 Convert News to RSS Portlet
-- ---------------------------
-- Add the titles from all existing news tools
INSERT INTO SAKAI_SITE_TOOL_PROPERTY (site_id, tool_id, name, value)
	SELECT site_id, tool_id, 'javax.portlet-portlet_title', title FROM SAKAI_SITE_TOOL WHERE registration = 'sakai.news';

-- Setup all instances with the URL
UPDATE SAKAI_SITE_TOOL_PROPERTY SET name = 'javax.portlet-feed_url' WHERE name = 'channel-url';

-- Finally, convert all news tools to the new portlet (must run last)
UPDATE SAKAI_SITE_TOOL SET registration = 'sakai.simple.rss' WHERE registration = 'sakai.news';
-- End SAK-25784


-- New permissions

-- KNL-1350 / SAK-11647
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'dropbox.maintain.own.groups');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain.own.groups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'dropbox.maintain.own.groups'));
-- END KNL-1350 / SAK-11647

INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.myGroupMembers');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.myGroups');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.allowToField.users');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'msg.permissions.viewHidden.groups');

-- Access 
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Maintain
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Access
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'access'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Maintain
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Secondary Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Grade Admin
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Course Coordinator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Content Designer
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Content Designer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Content Designer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Content Designer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Librarian
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Librarian'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Librarian'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Librarian'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Student
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Auditor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Auditor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Auditor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Auditor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- TA
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Coordinator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Participant
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'CIG Participant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Reviewer
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Reviewer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Evaluator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolio'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Evaluator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Prog Admin
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Prog Coord
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.portfolioAdmin'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Program Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Student
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Student'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Faculty
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Faculty'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Faculty'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Faculty'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Faculty'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Member
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Member'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Member'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Member'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Learner
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Learner'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Learner'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Learner'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Mentor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Mentor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Mentor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Mentor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Mentor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Staff
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Staff'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Staff'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Staff'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Staff'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Alumni
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Alumni'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Alumni'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Alumni'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Prospective Student
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'ProspectiveStudent'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'ProspectiveStudent'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'ProspectiveStudent'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Other
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Other'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Other'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Other'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Guest
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Guest'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));

-- Observer
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Observer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Observer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Observer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Observer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Admininstrator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroupMembers'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.myGroups'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.allowToField.users'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'msg.permissions.viewHidden.groups'));


-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permissions into existing realms
--
-- msg.permissions.allowToField.myGroupMembers
-- msg.permissions.allowToField.myGroups
-- msg.permissions.allowToField.users
-- msg.permissions.viewHidden.groups
-- dropbox.maintain.own.groups (Just for Teaching Assistant)
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- maintain
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','msg.permissions.viewHidden.groups');

-- access
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('access','msg.permissions.allowToField.users');

-- Instructor
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','msg.permissions.viewHidden.groups');

-- Secondary Instructor
INSERT INTO PERMISSIONS_SRC_TEMP values ('Secondary Instructor','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Secondary Instructor','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Secondary Instructor','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Secondary Instructor','msg.permissions.viewHidden.groups');

-- Grade Admin
INSERT INTO PERMISSIONS_SRC_TEMP values ('Grade Admin','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Grade Admin','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Grade Admin','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Grade Admin','msg.permissions.viewHidden.groups');

-- Course Coordinator
INSERT INTO PERMISSIONS_SRC_TEMP values ('Course Coordinator','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Course Coordinator','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Course Coordinator','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Course Coordinator','msg.permissions.viewHidden.groups');

-- Content Designer
INSERT INTO PERMISSIONS_SRC_TEMP values ('Content Designer','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Content Designer','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Content Designer','msg.permissions.allowToField.users');

-- Librarian
INSERT INTO PERMISSIONS_SRC_TEMP values ('Librarian','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Librarian','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Librarian','msg.permissions.allowToField.users');

-- TA
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Teaching Assistant','dropbox.maintain.own.groups');

-- Student
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Student','msg.permissions.allowToField.users');

-- Auditor
INSERT INTO PERMISSIONS_SRC_TEMP values ('Auditor','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Auditor','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Auditor','msg.permissions.allowToField.users');

-- CIG Coordinator
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Coordinator','msg.permissions.viewHidden.groups');

-- Evaluator
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Evaluator','msg.permissions.viewHidden.groups');

-- Reviewer
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Reviewer','msg.permissions.viewHidden.groups');

-- CIG Participant
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.myGroupMembers');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.myGroups');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.allowToField.users');
INSERT INTO PERMISSIONS_SRC_TEMP values ('CIG Participant','msg.permissions.viewHidden.groups');



-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" or "!user.template")
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

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;


-- ------------------------------
--  END permission backfill -----
-- ------------------------------

-- KNL-1336 - Add status for all nodes in a cluster.
ALTER TABLE SAKAI_CLUSTER ADD COLUMN STATUS VARCHAR(8);
-- We rename the column so we don't have update the primary key index
ALTER TABLE SAKAI_CLUSTER CHANGE SERVER_ID SERVER_ID_INSTANCE VARCHAR (64);
ALTER TABLE SAKAI_CLUSTER ADD COLUMN SERVER_ID VARCHAR (64);

-- SAK-27937 - Course grade to disable course points
ALTER TABLE GB_GRADEBOOK_T ADD COLUMN COURSE_POINTS_DISPLAYED bit(1) NOT NULL DEFAULT b'0';
-- End SAK-27937

-- SAK-25385
ALTER TABLE GB_GRADABLE_OBJECT_T MODIFY DUE_DATE DATETIME;
-- End SAK-25385

-- SAK-23666 Add OAuth Admin tool to Administration workspace
INSERT INTO SAKAI_SITE_PAGE VALUES ('!admin-1500', '!admin', 'OAuth Admin', '0', 20, '0');
INSERT INTO SAKAI_SITE_TOOL VALUES ('!admin-1550', '!admin-1500', '!admin', 'sakai.oauth.admin', 1, 'OAuth Admin', NULL);

-- SAK-23666 Add OAuth Admin tool to My Workspace template 
INSERT INTO SAKAI_SITE_PAGE VALUES ('!user-650', '!user', 'Trusted Applications', '0', 9, '0');
INSERT INTO SAKAI_SITE_TOOL VALUES ('!user-655', '!user-650', '!user', 'sakai.oauth', 1, 'Trusted Applications', NULL);

-- SAK-28084 Roles for adding .auth/.anon to a site.
INSERT INTO SAKAI_REALM VALUES (DEFAULT, '!site.roles', '', NULL, 'admin', 'admin', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.anon'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.auth'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));

-- SAK-29432 Add dropDate field to Enrollment
ALTER TABLE CM_ENROLLMENT_T ADD COLUMN DROP_DATE DATE;

-- SAK-29422 Incorporate NYU's "public announcement system"

CREATE TABLE `pasystem_popup_screens` (
  `uuid` CHAR(36) PRIMARY KEY,
  `descriptor` VARCHAR(255),
  `start_time` BIGINT,
  `end_time` BIGINT,
  `open_campaign` int(1) DEFAULT NULL,
  INDEX `start_time` (`start_time`),
  INDEX `descriptor` (`descriptor`)
);

CREATE TABLE `pasystem_popup_content` (
  `uuid` char(36) PRIMARY KEY,
  `template_content` MEDIUMTEXT,
  FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid)
);

CREATE TABLE `pasystem_popup_assign` (
  `uuid` char(36),
  `user_eid` varchar(255) DEFAULT NULL,
   FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid),
   INDEX `user_eid` (`user_eid`)
);

CREATE TABLE `pasystem_popup_dismissed` (
  `uuid` char(36),
  `user_eid` varchar(255) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `dismiss_time` BIGINT,
   UNIQUE KEY `unique_popup_dismissed` (`user_eid`,`state`, `uuid`),
   FOREIGN KEY (uuid) REFERENCES pasystem_popup_screens(uuid),
   INDEX `user_eid` (`user_eid`),
   INDEX `state` (`state`)
);


CREATE TABLE pasystem_banner_alert
( `uuid` CHAR(36) PRIMARY KEY,
  `message` VARCHAR(4000) NOT NULL,
  `hosts` VARCHAR(512) DEFAULT NULL,
  `active` INT(1) NOT NULL DEFAULT 0,
  `start_time` BIGINT DEFAULT NULL,
  `end_time` BIGINT DEFAULT NULL,
  `banner_type` VARCHAR(255) DEFAULT 'warning'
);

INSERT IGNORE INTO SAKAI_REALM_FUNCTION (FUNCTION_NAME) VALUES ('pasystem.manage');

CREATE TABLE `pasystem_banner_dismissed` (
  `uuid` char(36),
  `user_eid` varchar(255) DEFAULT NULL,
  `state` varchar(50) DEFAULT NULL,
  `dismiss_time` BIGINT,
   UNIQUE KEY `unique_banner_dismissed` (`user_eid`,`state`, `uuid`),
   FOREIGN KEY (uuid) REFERENCES pasystem_banner_alert(uuid),
   INDEX `user_eid` (`user_eid`),
   INDEX `state` (`state`)
);

INSERT INTO SAKAI_SITE_PAGE VALUES('!admin-1600', '!admin', 'PA System', '0', 20, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!admin-1650', '!admin-1600', '!admin', 'sakai.pasystem', 1, 'PA System', NULL );
INSERT INTO SAKAI_SITE_PAGE_PROPERTY VALUES('!admin', '!admin-1600', 'sitePage.customTitle', 'true');


-- END SAK-29422 Incorporate NYU's "public announcement system"

-- LSNBLDR-646
drop index lesson_builder_qr_questionId on lesson_builder_q_responses;
-- END LSNBLDR-646

-- KNL-1369 Update kernel DDL with new roster.viewsitevisits permission
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'roster.viewsitevisits');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!group.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Teaching Assistant'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'roster.viewsitevisits'));
-- END KNL-1369

-- SAK-29497 Improve usability of Schedule's "List of events" page
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'calendar.view.audience');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.view.audience'));
-- END SAK-29497

-- SAK-29271 Feedback Tool
CREATE TABLE IF NOT EXISTS sakai_feedback (
                id INT NOT NULL AUTO_INCREMENT,
                user_id VARCHAR(99),
                email VARCHAR(255) NOT NULL,
                site_id VARCHAR(99) NOT NULL,
                report_type ENUM('content','technical', 'helpdesk') NOT NULL,
                title VARCHAR(40) NOT NULL,
                content TEXT NOT NULL, PRIMARY KEY(id));
INSERT INTO SAKAI_SITE VALUES('!contact-us', 'Contact Us', null, null, null, '', '', null, 1, 0, 0, '', 'admin', 'admin', NOW(), NOW(), 1, 0, 0, 0, null);
INSERT INTO SAKAI_SITE_PAGE VALUES('!contact-us', '!contact-us', 'Contact Us', '0', 1, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!contact-us', '!contact-us', '!contact-us', 'sakai.feedback', 1, 'Contact Us', NULL );
-- END SAK-29271

-- SAK-29733 Change Schedule to Calendar for existing sites
UPDATE SAKAI_SITE_TOOL SET TITLE="Calendar" WHERE REGISTRATION = "sakai.schedule" AND TITLE = "Schedule";
UPDATE SAKAI_SITE_PAGE SET TITLE="Calendar" WHERE TITLE = "Schedule";

-- SAK-29974 Nested citation lists
ALTER TABLE CITATION_COLLECTION_ORDER ADD SECTION_TYPE ENUM('HEADING1','HEADING2', 'HEADING3', 'DESCRIPTION', 'CITATION') DEFAULT NULL;
ALTER TABLE CITATION_COLLECTION_ORDER ADD VALUE TEXT DEFAULT NULL;
ALTER TABLE CITATION_COLLECTION_ORDER MODIFY COLUMN CITATION_ID VARCHAR(36) NULL;
-- End SAK-29974

--  SAK-30000 Site creation notification email template updates
UPDATE EMAIL_TEMPLATE_ITEM
SET message = '
From Worksite Setup to ${serviceName} support:

<#if courseSite ="true">Official Course Site<#else>Site </#if> ${siteTitle} (ID ${siteId}) was set up by ${currentUserDisplayName} (${currentUserDisplayId}, email ${currentUserEmail}) on ${dateDisplay} <#if courseSite ="true">for ${termTitle} </#if>
<#if numSections = "1">with access to the roster for this section:<#elseif numSections != "0">with access to rosters for these ${numSections} sections:</#if>

${sections}
'
WHERE template_key = 'sitemanage.notifySiteCreation' AND template_locale = 'default';
UPDATE EMAIL_TEMPLATE_ITEM
SET subject = 'Site "${siteTitle}" was successfully created by ${currentUserDisplayName}', message = '
Hi, ${currentUserDisplayName}:

Your site "${siteTitle}" has been successfully created. The following is a copy of the site creation notification email sent to ${serviceName} support:


From Worksite Setup to ${serviceName} support:

<#if courseSite ="true">Official Course Site<#else>Site </#if> ${siteTitle} (ID ${siteId}) was set up by ${currentUserDisplayName} (${currentUserDisplayId}, email ${currentUserEmail}) on ${dateDisplay} <#if courseSite ="true">for ${termTitle} </#if>
<#if numSections = "1">with access to the roster for this section:<#elseif numSections != "0">with access to rosters for these ${numSections} sections:</#if>

${sections}
'
WHERE template_key = 'sitemanage.notifySiteCreation.confirmation' AND template_locale = 'default';
-- END SAK-30000

-- SAK-29740 update gradebook settings
ALTER TABLE GB_GRADEBOOK_T ADD course_letter_grade_displayed BIT(1) NOT NULL DEFAULT true;

-- SAK-29401/SAK-29977 Role based access to sites --
INSERT INTO SAKAI_REALM_ROLE VALUES (DEFAULT, '.default');
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'annc.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'calendar.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'chat.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'content.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'mail.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'rwiki.read'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.roles'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = '.default'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'site.visit'));

-- SAM-2627
ALTER TABLE SAM_GRADINGATTACHMENT_T ADD COLUMN `ASSESSMENTGRADINGID` BIGINT(20) NULL AFTER `ITEMGRADINGID`;

-- SAK-25544/SAK-31365 - Quartz updated to 2.2.1
--
-- drop tables that are no longer used
--
DROP TABLE QRTZ_JOB_LISTENERS;
DROP TABLE QRTZ_TRIGGER_LISTENERS;
--
-- drop columns that are no longer used
--
ALTER TABLE QRTZ_FIRED_TRIGGERS DROP COLUMN IS_VOLATILE;
--
-- add new columns that replace the 'is_stateful' column
--
ALTER TABLE QRTZ_FIRED_TRIGGERS ADD COLUMN IS_NONCONCURRENT BOOL;
UPDATE QRTZ_FIRED_TRIGGERS SET IS_NONCONCURRENT = IS_STATEFUL;
ALTER TABLE QRTZ_FIRED_TRIGGERS DROP COLUMN IS_STATEFUL;
--
-- add new 'sched_name' column to all tables
--
ALTER TABLE QRTZ_CALENDARS ADD COLUMN SCHED_NAME VARCHAR(120) NOT NULL DEFAULT 'QUARTZSCHEDULER';
ALTER TABLE QRTZ_FIRED_TRIGGERS ADD COLUMN SCHED_NAME VARCHAR(120) NOT NULL DEFAULT 'QUARTZSCHEDULER';
ALTER TABLE QRTZ_LOCKS ADD COLUMN SCHED_NAME VARCHAR(120) NOT NULL DEFAULT 'QUARTZSCHEDULER';
ALTER TABLE QRTZ_PAUSED_TRIGGER_GRPS ADD COLUMN SCHED_NAME VARCHAR(120) NOT NULL DEFAULT 'QUARTZSCHEDULER';
ALTER TABLE QRTZ_SCHEDULER_STATE ADD COLUMN SCHED_NAME VARCHAR(120) NOT NULL DEFAULT 'QUARTZSCHEDULER';
--
-- add new 'sched_time' column to all tables
--
ALTER TABLE QRTZ_FIRED_TRIGGERS ADD COLUMN SCHED_TIME BIGINT(13) NOT NULL;
--
-- drop intermediate tables
--
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS_NEW;
DROP TABLE IF EXISTS QRTZ_TRIGGERS_NEW;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS_NEW;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS_NEW;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS_NEW;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS_OLD;
DROP TABLE IF EXISTS QRTZ_TRIGGERS_OLD;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS_OLD;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS_OLD;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS_OLD;
SET FOREIGN_KEY_CHECKS = 1;
--
-- create new tables
--
CREATE TABLE QRTZ_JOB_DETAILS_NEW (
SCHED_NAME VARCHAR(120) NOT NULL,
JOB_NAME VARCHAR(200) NOT NULL,
JOB_GROUP VARCHAR(200) NOT NULL,
DESCRIPTION VARCHAR(250) NULL,
JOB_CLASS_NAME VARCHAR(250) NOT NULL,
IS_DURABLE VARCHAR(1) NOT NULL,
IS_NONCONCURRENT VARCHAR(1) NOT NULL,
IS_UPDATE_DATA VARCHAR(1) NOT NULL,
REQUESTS_RECOVERY VARCHAR(1) NOT NULL,
JOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,JOB_NAME,JOB_GROUP))
ENGINE=INNODB;

CREATE TABLE QRTZ_TRIGGERS_NEW (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
JOB_NAME VARCHAR(200) NOT NULL,
JOB_GROUP VARCHAR(200) NOT NULL,
DESCRIPTION VARCHAR(250) NULL,
NEXT_FIRE_TIME BIGINT(13) NULL,
PREV_FIRE_TIME BIGINT(13) NULL,
PRIORITY INTEGER NULL,
TRIGGER_STATE VARCHAR(16) NOT NULL,
TRIGGER_TYPE VARCHAR(8) NOT NULL,
START_TIME BIGINT(13) NOT NULL,
END_TIME BIGINT(13) NULL,
CALENDAR_NAME VARCHAR(200) NULL,
MISFIRE_INSTR SMALLINT(2) NULL,
JOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=INNODB;

CREATE TABLE QRTZ_SIMPLE_TRIGGERS_NEW (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
REPEAT_COUNT BIGINT(7) NOT NULL,
REPEAT_INTERVAL BIGINT(12) NOT NULL,
TIMES_TRIGGERED BIGINT(10) NOT NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=INNODB;

CREATE TABLE QRTZ_CRON_TRIGGERS_NEW (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
CRON_EXPRESSION VARCHAR(120) NOT NULL,
TIME_ZONE_ID VARCHAR(80),
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=INNODB;

CREATE TABLE QRTZ_BLOB_TRIGGERS_NEW (
SCHED_NAME VARCHAR(120) NOT NULL,
TRIGGER_NAME VARCHAR(200) NOT NULL,
TRIGGER_GROUP VARCHAR(200) NOT NULL,
BLOB_DATA BLOB NULL,
PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP))
ENGINE=INNODB;
--
-- Copy data from old tables into new
--
INSERT INTO QRTZ_JOB_DETAILS_NEW (SCHED_NAME, JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_NONCONCURRENT, IS_UPDATE_DATA, REQUESTS_RECOVERY, JOB_DATA) SELECT 'QUARTZSCHEDULER', JOB_NAME, JOB_GROUP, DESCRIPTION, JOB_CLASS_NAME, IS_DURABLE, IS_STATEFUL, IS_STATEFUL, REQUESTS_RECOVERY, JOB_DATA FROM QRTZ_JOB_DETAILS;
INSERT INTO QRTZ_TRIGGERS_NEW (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, PRIORITY, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR, JOB_DATA) SELECT 'QUARTZSCHEDULER', TRIGGER_NAME, TRIGGER_GROUP, JOB_NAME, JOB_GROUP, DESCRIPTION, NEXT_FIRE_TIME, PREV_FIRE_TIME, PRIORITY, TRIGGER_STATE, TRIGGER_TYPE, START_TIME, END_TIME, CALENDAR_NAME, MISFIRE_INSTR, JOB_DATA FROM QRTZ_TRIGGERS;
INSERT INTO QRTZ_SIMPLE_TRIGGERS_NEW (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, REPEAT_COUNT, REPEAT_INTERVAL, TIMES_TRIGGERED) SELECT 'QUARTZSCHEDULER', TRIGGER_NAME, TRIGGER_GROUP, REPEAT_COUNT, REPEAT_INTERVAL, TIMES_TRIGGERED FROM QRTZ_SIMPLE_TRIGGERS;
INSERT INTO QRTZ_CRON_TRIGGERS_NEW (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, CRON_EXPRESSION, TIME_ZONE_ID) SELECT 'QUARTZSCHEDULER', TRIGGER_NAME, TRIGGER_GROUP, CRON_EXPRESSION, TIME_ZONE_ID FROM QRTZ_CRON_TRIGGERS;
INSERT INTO QRTZ_BLOB_TRIGGERS_NEW (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP, BLOB_DATA) SELECT 'QUARTZSCHEDULER', TRIGGER_NAME, TRIGGER_GROUP, BLOB_DATA FROM QRTZ_BLOB_TRIGGERS;
--
-- Rename tables
--
RENAME TABLE QRTZ_JOB_DETAILS TO QRTZ_JOB_DETAILS_OLD, QRTZ_JOB_DETAILS_NEW TO QRTZ_JOB_DETAILS;
RENAME TABLE QRTZ_TRIGGERS TO QRTZ_TRIGGERS_OLD, QRTZ_TRIGGERS_NEW TO QRTZ_TRIGGERS;
RENAME TABLE QRTZ_SIMPLE_TRIGGERS TO QRTZ_SIMPLE_TRIGGERS_OLD, QRTZ_SIMPLE_TRIGGERS_NEW TO QRTZ_SIMPLE_TRIGGERS;
RENAME TABLE QRTZ_CRON_TRIGGERS TO QRTZ_CRON_TRIGGERS_OLD, QRTZ_CRON_TRIGGERS_NEW TO QRTZ_CRON_TRIGGERS;
RENAME TABLE QRTZ_BLOB_TRIGGERS TO QRTZ_BLOB_TRIGGERS_OLD, QRTZ_BLOB_TRIGGERS_NEW TO QRTZ_BLOB_TRIGGERS;
--
-- Drop old tables
--
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS QRTZ_JOB_DETAILS_OLD;
DROP TABLE IF EXISTS QRTZ_TRIGGERS_OLD;
DROP TABLE IF EXISTS QRTZ_SIMPLE_TRIGGERS_OLD;
DROP TABLE IF EXISTS QRTZ_CRON_TRIGGERS_OLD;
DROP TABLE IF EXISTS QRTZ_BLOB_TRIGGERS_OLD;
SET FOREIGN_KEY_CHECKS = 1;
--
-- Add keys
--
ALTER TABLE QRTZ_TRIGGERS ADD FOREIGN KEY (SCHED_NAME, JOB_NAME, JOB_GROUP) REFERENCES QRTZ_JOB_DETAILS (SCHED_NAME, JOB_NAME, JOB_GROUP);
ALTER TABLE QRTZ_BLOB_TRIGGERS ADD FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
ALTER TABLE QRTZ_CRON_TRIGGERS ADD FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
ALTER TABLE QRTZ_SIMPLE_TRIGGERS ADD FOREIGN KEY (SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP) REFERENCES QRTZ_TRIGGERS(SCHED_NAME, TRIGGER_NAME, TRIGGER_GROUP);
ALTER TABLE QRTZ_CALENDARS DROP PRIMARY KEY, ADD PRIMARY KEY (SCHED_NAME, CALENDAR_NAME);
ALTER TABLE QRTZ_LOCKS DROP PRIMARY KEY, ADD PRIMARY KEY (SCHED_NAME, LOCK_NAME);
ALTER TABLE QRTZ_PAUSED_TRIGGER_GRPS DROP PRIMARY KEY, ADD PRIMARY KEY (SCHED_NAME, TRIGGER_GROUP);
ALTER TABLE QRTZ_SCHEDULER_STATE DROP PRIMARY KEY, ADD PRIMARY KEY (SCHED_NAME, INSTANCE_NAME);
--
-- add new simprop_triggers table
--
CREATE TABLE QRTZ_SIMPROP_TRIGGERS
 (
    SCHED_NAME VARCHAR(120) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    STR_PROP_1 VARCHAR(512) NULL,
    STR_PROP_2 VARCHAR(512) NULL,
    STR_PROP_3 VARCHAR(512) NULL,
    INT_PROP_1 INT NULL,
    INT_PROP_2 INT NULL,
    LONG_PROP_1 BIGINT NULL,
    LONG_PROP_2 BIGINT NULL,
    DEC_PROP_1 NUMERIC(13,4) NULL,
    DEC_PROP_2 NUMERIC(13,4) NULL,
    BOOL_PROP_1 BOOL NULL,
    BOOL_PROP_2 BOOL NULL,
    PRIMARY KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
    REFERENCES QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP)
);
--
-- create indexes for faster queries
--
CREATE INDEX IDX_QRTZ_J_REQ_RECOVERY ON QRTZ_JOB_DETAILS(SCHED_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_J_GRP ON QRTZ_JOB_DETAILS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_J ON QRTZ_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_JG ON QRTZ_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_T_C ON QRTZ_TRIGGERS(SCHED_NAME,CALENDAR_NAME);
CREATE INDEX IDX_QRTZ_T_G ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_N_G_STATE ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON QRTZ_TRIGGERS(SCHED_NAME,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST ON QRTZ_TRIGGERS(SCHED_NAME,TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NFT_ST_MISFIRE_GRP ON QRTZ_TRIGGERS(SCHED_NAME,MISFIRE_INSTR,NEXT_FIRE_TIME,TRIGGER_GROUP,TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_INST_JOB_REQ_RCVRY ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,INSTANCE_NAME,REQUESTS_RECOVERY);
CREATE INDEX IDX_QRTZ_FT_J_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_T_G ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TG ON QRTZ_FIRED_TRIGGERS(SCHED_NAME,TRIGGER_GROUP);

--
-- SAM-948 - MIN_SCORE option in Samigo
--

alter table SAM_ITEM_T add column MIN_SCORE double NULL;
alter table SAM_PUBLISHEDITEM_T add column MIN_SCORE double NULL;

-- KNL-1405 Don't have defaults for TIMESTAMP in SAKAI_SESSION
ALTER TABLE SAKAI_SESSION MODIFY SESSION_START TIMESTAMP NULL;
ALTER TABLE SAKAI_SESSION MODIFY SESSION_END TIMESTAMP NULL;

-- 1389 GradebookNG sortable assignments within categories, add CATEGORIZED_SORT_ORDER to GB_GRADABLE_OBJECT_T
ALTER TABLE GB_GRADABLE_OBJECT_T ADD COLUMN CATEGORIZED_SORT_ORDER int NULL;
-- 1840 Allow quick queries of grading events by date graded
CREATE INDEX GB_GRADING_EVENT_T_DATE_OBJ_ID ON GB_GRADING_EVENT_T (DATE_GRADED, GRADABLE_OBJECT_ID);
--
-- SAM-1117 - Option to not show score
--

ALTER TABLE SAM_ASSESSACCESSCONTROL_T ADD COLUMN DISPLAYSCORE integer;
ALTER TABLE SAM_PUBLISHEDACCESSCONTROL_T ADD COLUMN DISPLAYSCORE integer;

ALTER TABLE SAM_ITEM_T ADD COLUMN SCORE_DISPLAY_FLAG bit(1);
ALTER TABLE SAM_PUBLISHEDITEM_T ADD COLUMN SCORE_DISPLAY_FLAG bit(1);

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, 1, 'displayScores_isInstructorEditable', 'true') ;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL,
    ENTRY)
    VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
     AND TYPEID='142' AND ISTEMPLATE=1),
      'displayScores_isInstructorEditable', 'true');

-- LTI CHANGES !!!
ALTER TABLE lti_binding MODIFY settings MEDIUMTEXT;

alter table lti_content add FA_ICON varchar(1024);
alter table lti_content add CONTENTITEM mediumtext;
alter table lti_tools add pl_launch tinyint(4) default 0;
alter table lti_tools add pl_linkselection tinyint(4) default 0;
alter table lti_tools add pl_fileitem tinyint(4) default 0;
alter table lti_tools add pl_contenteditor tinyint(4) default 0;
alter table lti_tools add pl_assessmentselection tinyint(4) default 0;
alter table lti_tools add pl_importitem tinyint(4) default 0;
alter table lti_tools add fa_icon varchar(1024);
alter table lti_tools add tool_proxy_binding mediumtext;
ALTER TABLE lti_tools MODIFY settings MEDIUMTEXT;
ALTER TABLE lti_tools MODIFY xmlimport MEDIUMTEXT;

ALTER TABLE lti_content MODIFY title VARCHAR(1024);
ALTER TABLE lti_content MODIFY pagetitle VARCHAR(1024);
ALTER TABLE lti_content MODIFY consumerkey VARCHAR(1024);
ALTER TABLE lti_content MODIFY secret VARCHAR(1024);
-- alter table lti_content modify custom varchar(65536);
ALTER TABLE lti_content MODIFY xmlimport MEDIUMTEXT;
ALTER TABLE lti_content MODIFY settings MEDIUMTEXT;
ALTER TABLE lti_content MODIFY settings_ext MEDIUMTEXT;

ALTER TABLE lti_tools MODIFY title VARCHAR(1024);
ALTER TABLE lti_tools MODIFY pagetitle VARCHAR(1024);
ALTER TABLE lti_tools MODIFY consumerkey VARCHAR(1024);
ALTER TABLE lti_tools MODIFY secret VARCHAR(1024);
-- alter table lti_tools modify custom varchar(65536);

ALTER TABLE lti_deploy MODIFY reg_profile MEDIUMTEXT;
ALTER TABLE lti_deploy MODIFY settings MEDIUMTEXT;
ALTER TABLE lti_deploy MODIFY title VARCHAR(1024);
ALTER TABLE lti_deploy MODIFY pagetitle VARCHAR(1024);
ALTER TABLE lti_deploy ADD allowcontentitem tinyint(4) DEFAULT 0;
ALTER TABLE lti_deploy MODIFY reg_key VARCHAR(1024);
ALTER TABLE lti_deploy MODIFY reg_password VARCHAR(1024);
ALTER TABLE lti_deploy ADD reg_ack TEXT;
ALTER TABLE lti_deploy MODIFY consumerkey VARCHAR(1024);
ALTER TABLE lti_deploy MODIFY secret VARCHAR(1024);
ALTER TABLE lti_deploy ADD new_secret VARCHAR(1024);

CREATE TABLE lti_memberships_jobs (
    SITE_ID VARCHAR(99),
    memberships_id VARCHAR(256),
    memberships_url mediumtext,
    consumerkey VARCHAR(1024),
    lti_version VARCHAR(32)
);
-- END LTI CHANGES !!

-- LSNBLDR-622
alter table lesson_builder_items modify column name varchar(255);
alter table lesson_builder_pages modify column title varchar(255);
alter table lesson_builder_p_eval_results modify column gradee varchar(99) null;                                     
alter table lesson_builder_p_eval_results modify column row_text varchar(255) null;                                  
alter table lesson_builder_p_eval_results add column gradee_group varchar(99) null;
alter table lesson_builder_p_eval_results add column row_id  bigint(20) default 0;
-- sites new with 10 will already have this but it was missing in 10 conversion scripts
alter table lesson_builder_groups modify column groups longtext;
create table lesson_builder_ch_status (
        checklistId bigint(20) not null,
        checklistItemId bigint(20) not null,
        owner varchar(99) not null,
        done bit(1),
        primary key (checklistId,checklistItemId,owner)
 );
create index lesson_builder_p_eval_res_row on lesson_builder_p_eval_results(page_id);

-- ---------------------------------------------------------------------------
-- SAKAI_CONFIG_ITEM - KNL-1063 - MYSQL
-- ---------------------------------------------------------------------------

CREATE TABLE SAKAI_CONFIG_ITEM (
	ID				BIGINT(20) NOT NULL AUTO_INCREMENT,
	NODE			VARCHAR(255),
	NAME			VARCHAR(255) NOT NULL,
	VALUE			LONGTEXT,  
	RAW_VALUE		LONGTEXT,
	TYPE			VARCHAR(255) NOT NULL,
	DEFAULT_VALUE	LONGTEXT,
	DESCRIPTION		LONGTEXT,
	SOURCE			VARCHAR(255) DEFAULT NULL,
	DEFAULTED		BIT(1) NOT NULL,
	REGISTERED		BIT(1) NOT NULL,
	SECURED			BIT(1) NOT NULL,
	DYNAMIC			BIT(1) NOT NULL,
	CREATED			DATETIME NOT NULL,
	MODIFIED		DATETIME NOT NULL,
	POLL_ON			DATETIME DEFAULT NULL,
	PRIMARY KEY (ID)
);

CREATE INDEX SCI_NODE_IDX ON SAKAI_CONFIG_ITEM (NODE ASC);
CREATE INDEX SCI_NAME_IDX ON SAKAI_CONFIG_ITEM (NAME ASC);

-- SAK-30032 Create table to handle Peer Review attachments --
CREATE TABLE ASN_PEER_ASSESSMENT_ATTACH_T (
ID int NOT NULL AUTO_INCREMENT,
SUBMISSION_ID varchar(255) NOT NULL, 
ASSESSOR_USER_ID varchar(255) NOT NULL,
RESOURCE_ID varchar(255) NOT NULL, 
PRIMARY KEY(ID)
);
create index PEER_ASSESSOR_I on ASN_PEER_ASSESSMENT_ATTACH_T (SUBMISSION_ID, ASSESSOR_USER_ID);
-- END SAK-30032

-- KNL-1424 Add Message Bundle Manager to admin workspace
INSERT INTO SAKAI_SITE_PAGE VALUES('!admin-1575', '!admin', 'Message Bundle Manager', '0', 21, '0' );
INSERT INTO SAKAI_SITE_TOOL VALUES('!admin-1575', '!admin-1575', '!admin', 'sakai.message.bundle.manager', 1, 'Message Bundle Manager', NULL );
INSERT INTO SAKAI_SITE_PAGE_PROPERTY VALUES('!admin', '!admin-1575', 'sitePage.customTitle', 'true');
-- END KNL-1424

-- SAM-2709 Submission Email Notifications Hidden Inappropriately--
-- ALTER TABLE SAM_ASSESSACCESSCONTROL_T ADD COLUMN INSTRUCTORNOTIFICATION integer;
-- ALTER TABLE SAM_PUBLISHEDACCESSCONTROL_T ADD COLUMN INSTRUCTORNOTIFICATION integer;

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY)
    VALUES(NULL, 1, 'instructorNotification_isInstructorEditable', 'true') ;

INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY)
     VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Formative Assessment'
      AND TYPEID='142' AND ISTEMPLATE=1),
       'instructorNotification_isInstructorEditable', 'true');
 
 INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY)
     VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Quiz'
      AND TYPEID='142' AND ISTEMPLATE=1),
       'instructorNotification_isInstructorEditable', 'true');
 
 INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY)
     VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Problem Set'
      AND TYPEID='142' AND ISTEMPLATE=1),
       'instructorNotification_isInstructorEditable', 'true');
 
 INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY)
     VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Survey'
      AND TYPEID='142' AND ISTEMPLATE=1),
       'instructorNotification_isInstructorEditable', 'true');
 
 INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY)
     VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Test'
      AND TYPEID='142' AND ISTEMPLATE=1),
       'instructorNotification_isInstructorEditable', 'true');
 
 INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTMETADATAID, ASSESSMENTID, LABEL, ENTRY)
     VALUES(NULL, (SELECT ID FROM SAM_ASSESSMENTBASE_T WHERE TITLE='Timed Test'
      AND TYPEID='142' AND ISTEMPLATE=1),
       'instructorNotification_isInstructorEditable', 'true');
-- END SAM-2709

-- SAM-2751
ALTER TABLE SAM_ASSESSACCESSCONTROL_T ADD HONORPLEDGE BIT;
ALTER TABLE SAM_PUBLISHEDACCESSCONTROL_T ADD HONORPLEDGE BIT;
INSERT INTO SAM_ASSESSMETADATA_T (ASSESSMENTID, LABEL, ENTRY)
     SELECT DISTINCT ASSESSMENTID, 'honorpledge_isInstructorEditable' as LABEL, 'true' as ENTRY
       FROM SAM_ASSESSMETADATA_T WHERE ASSESSMENTID NOT IN
         (SELECT DISTINCT ASSESSMENTID FROM SAM_ASSESSMETADATA_T WHERE LABEL = 'honorpledge_isInstructorEditable');
-- END SAM-2751

-- SAM-1200 - Increase column data sizes
alter table SAM_PUBLISHEDASSESSMENT_T change description description mediumtext null;
alter table SAM_PUBLISHEDSECTION_T change description description mediumtext null;
alter table SAM_ASSESSMENTBASE_T change description description mediumtext null;
alter table SAM_SECTION_T change description description mediumtext null;
alter table SAM_ITEMGRADING_T change comments comments mediumtext null;
alter table SAM_ASSESSMENTGRADING_T change comments comments mediumtext null;
-- END SAM-1200

CREATE TABLE MFR_RANK_INDIVIDUAL_T (
  RANK_ID bigint(20) NOT NULL,
  USER_ID varchar(99) NOT NULL,
  PRIMARY KEY (RANK_ID,USER_ID),
  KEY mfr_rank_indiv_fk (RANK_ID),
  CONSTRAINT mfr_rank_indiv_fk FOREIGN KEY (RANK_ID) REFERENCES MFR_RANK_T (ID)
);


-- New permissions for SAK-30141
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'syllabus.add.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'syllabus.bulk.add.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'syllabus.bulk.edit.item');
INSERT INTO SAKAI_REALM_FUNCTION VALUES (DEFAULT, 'syllabus.redirect');

-- Maintain
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.user'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'maintain'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Secondary Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Secondary Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Grade Admin
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Grade Admin'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Course Coordinator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Course Coordinator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Content Designer
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Content Designer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Content Designer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Content Designer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Content Designer'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Librarian
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Librarian'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Librarian'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Librarian'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.course'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Librarian'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Admininstrator
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Administrator'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));

-- Instructor
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.add.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.bulk.edit.item'));
INSERT INTO SAKAI_REALM_RL_FN VALUES((select REALM_KEY from SAKAI_REALM where REALM_ID = '!site.template.lti'), (select ROLE_KEY from SAKAI_REALM_ROLE where ROLE_NAME = 'Instructor'), (select FUNCTION_KEY from SAKAI_REALM_FUNCTION where FUNCTION_NAME = 'syllabus.redirect'));


-- --------------------------------------------------------------------------------------------------------------------------------------
-- backfill new permissions into existing realms
--
-- syllabus.add.item
-- syllabus.bulk.add.item
-- syllabus.bulk.edit.item
-- syllabus.redirect
-- --------------------------------------------------------------------------------------------------------------------------------------

-- for each realm that has a role matching something in this table, we will add to that role the function from this table
CREATE TABLE PERMISSIONS_SRC_TEMP (ROLE_NAME VARCHAR(99), FUNCTION_NAME VARCHAR(99));

-- maintain
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('maintain','syllabus.redirect');

-- Instructor
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Instructor','syllabus.redirect');

-- Secondary Instructor
INSERT INTO PERMISSIONS_SRC_TEMP values ('Secondary Instructor','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Secondary Instructor','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Secondary Instructor','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Secondary Instructor','syllabus.redirect');

-- Grade Admin
INSERT INTO PERMISSIONS_SRC_TEMP values ('Grade Admin','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Grade Admin','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Grade Admin','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Grade Admin','syllabus.redirect');

-- Course Coordinator
INSERT INTO PERMISSIONS_SRC_TEMP values ('Course Coordinator','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Course Coordinator','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Course Coordinator','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Course Coordinator','syllabus.redirect');

-- Content Designer
INSERT INTO PERMISSIONS_SRC_TEMP values ('Content Designer','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Content Designer','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Content Designer','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Content Designer','syllabus.redirect');

-- Librarian
INSERT INTO PERMISSIONS_SRC_TEMP values ('Librarian','syllabus.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Librarian','syllabus.bulk.add.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Librarian','syllabus.bulk.edit.item');
INSERT INTO PERMISSIONS_SRC_TEMP values ('Librarian','syllabus.redirect');

-- lookup the role and function numbers
CREATE TABLE PERMISSIONS_TEMP (ROLE_KEY INTEGER, FUNCTION_KEY INTEGER);
INSERT INTO PERMISSIONS_TEMP (ROLE_KEY, FUNCTION_KEY)
SELECT SRR.ROLE_KEY, SRF.FUNCTION_KEY
from PERMISSIONS_SRC_TEMP TMPSRC
JOIN SAKAI_REALM_ROLE SRR ON (TMPSRC.ROLE_NAME = SRR.ROLE_NAME)
JOIN SAKAI_REALM_FUNCTION SRF ON (TMPSRC.FUNCTION_NAME = SRF.FUNCTION_NAME);

-- insert the new functions into the roles of any existing realm that has the role (don't convert the "!site.helper" or "!user.template")
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

-- clean up the temp tables
DROP TABLE PERMISSIONS_TEMP;
DROP TABLE PERMISSIONS_SRC_TEMP;

-- ------------------------------
--  END permission backfill -----
-- ------------------------------

-- End permissions for SAK-30141

-- SAK-30144: Add the new 'EID' column to the VALIDATIONACCOUNT_ITEM table
ALTER TABLE VALIDATIONACCOUNT_ITEM ADD COLUMN EID VARCHAR(255);

-- SAK-31468 rename existing gradebooks to 'Gradebook Classic'
-- This will not change any tool placements. To do that, uncomment the following line:
-- UPDATE SAKAI_SITE_TOOL SET REGISTRATION='sakai.gradebookng' WHERE REGISTRATION='sakai.gradebook.tool';
UPDATE SAKAI_SITE_TOOL SET TITLE='Gradebook Classic' WHERE TITLE='Gradebook';
UPDATE SAKAI_SITE_PAGE SET TITLE='Gradebook Classic' WHERE TITLE='Gradebook';


-- 11.0 -> 11.1 -------------------------------------------------------

-- SAK-23666/SAK-31603

create table OAUTH_ACCESSORS (token varchar(255) not null, secret varchar(255), consumerId varchar(255), userId varchar(255), callbackUrl varchar(255), verifier varchar(255), creationDate datetime, expirationDate datetime, status integer, type integer, accessorSecret varchar(255), primary key (token));
create table OAUTH_CONSUMERS (id varchar(255) not null, name varchar(255) not null, description varchar(255), url varchar(255), callbackUrl varchar(255), secret varchar(255) not null, accessorSecret varchar(255), recordModeEnabled bit, defaultValidity integer not null, primary key (id));
create table OAUTH_RIGHTS (id varchar(255) not null, accessright varchar(255) not null, primary key (id, accessright));
create index user_idx on OAUTH_ACCESSORS (userId);
alter table OAUTH_RIGHTS add index FK5992789F74F42154 (id), add constraint FK5992789F74F42154 foreign key (id) references OAUTH_CONSUMERS (id);

--
-- SAK-31636 Rename existing 'Home' tools
--

update SAKAI_SITE_PAGE set title = 'Overview' where title = 'Home';


-- 11.1 -> 11.2 ----------------------------------------------------------

-- #3258 Drop this unused column
ALTER TABLE gb_grade_record_t DROP COLUMN user_entered_grade;

-- SAM-3040 slow query observed
ALTER TABLE SAM_ASSESSMETADATA_T MODIFY COLUMN `LABEL` varchar(99), ADD INDEX `SAM_METADATA_IDX` (`LABEL`, `ENTRY`) ;
ALTER TABLE SAM_PUBLISHEDMETADATA_T MODIFY COLUMN `LABEL` varchar(99), ADD INDEX `SAM_PUBMETADATA_IDX` (`LABEL`, `ENTRY`) ;
-- END SAM-3040

-- SAK-31905
-- Uncomment this if you're using a standard Sakai GroupProvider/CourseManagement
-- If you're using a custom implementation you need to check what separator is used between provider IDs and if you
-- are using anything other than "," this uncomment this and update to use your separator
UPDATE SAKAI_REALM SET PROVIDER_ID = REPLACE(PROVIDER_ID, ',', '+') where PROVIDER_ID like '%,%';

-- You do want to run this part of it which fixes any older role IDs to use the correct separator.
UPDATE SAKAI_SITE_GROUP_PROPERTY SET value = REPLACE(value, '+', ',')
  WHERE name = 'group_prop_role_providerid' AND value LIKE '%+%';
-- END SAK-31905

-- OWL-2663
-- Rename old tables that are no longer in use. We will run in production with the tables renamed for a while
-- to ensure nothing breaks before we permanently remove them from the databases.

-- Rename DW_* tables
RENAME TABLE DW_ASSIGNMENT_STATUS TO OLD__DW_ASSIGNMENT_STATUS;
RENAME TABLE DW_CONTENT_RESOURCE_LOCK TO OLD__DW_CONTENT_RESOURCE_LOCK;
RENAME TABLE DW_GUIDANCE TO OLD__DW_GUIDANCE;
RENAME TABLE DW_GUIDANCE_ITEM TO OLD__DW_GUIDANCE_ITEM;
RENAME TABLE DW_GUIDANCE_ITEM_FILE TO OLD__DW_GUIDANCE_ITEM_FILE;
RENAME TABLE DW_MATRIX TO OLD__DW_MATRIX;
RENAME TABLE DW_MATRIX_CELL TO OLD__DW_MATRIX_CELL;
RENAME TABLE DW_METAOBJ_FORM_DEF TO OLD__DW_METAOBJ_FORM_DEF;
RENAME TABLE DW_PRES_ITEMDEF_MIMETYPE TO OLD__DW_PRES_ITEMDEF_MIMETYPE;
RENAME TABLE DW_PRESENTATION TO OLD__DW_PRESENTATION;
RENAME TABLE DW_PRESENTATION_COMMENT TO OLD__DW_PRESENTATION_COMMENT;
RENAME TABLE DW_PRESENTATION_ITEM TO OLD__DW_PRESENTATION_ITEM;
RENAME TABLE DW_PRESENTATION_ITEM_DEF TO OLD__DW_PRESENTATION_ITEM_DEF;
RENAME TABLE DW_PRESENTATION_ITEM_PROPERTY TO OLD__DW_PRESENTATION_ITEM_PROPERTY;
RENAME TABLE DW_PRESENTATION_LAYOUT TO OLD__DW_PRESENTATION_LAYOUT;
RENAME TABLE DW_PRESENTATION_LOG TO OLD__DW_PRESENTATION_LOG;
RENAME TABLE DW_PRESENTATION_PAGE TO OLD__DW_PRESENTATION_PAGE;
RENAME TABLE DW_PRESENTATION_PAGE_ITEM TO OLD__DW_PRESENTATION_PAGE_ITEM;
RENAME TABLE DW_PRESENTATION_PAGE_REGION TO OLD__DW_PRESENTATION_PAGE_REGION;
RENAME TABLE DW_PRESENTATION_TEMPLATE TO OLD__DW_PRESENTATION_TEMPLATE;
RENAME TABLE DW_RESOURCE TO OLD__DW_RESOURCE;
RENAME TABLE DW_RESOURCE_COLLECTION TO OLD__DW_RESOURCE_COLLECTION;
RENAME TABLE DW_REVIEW_ITEMS TO OLD__DW_REVIEW_ITEMS;
RENAME TABLE DW_SCAFFOLDING TO OLD__DW_SCAFFOLDING;
RENAME TABLE DW_SCAFFOLDING_CELL TO OLD__DW_SCAFFOLDING_CELL;
RENAME TABLE DW_SCAFFOLDING_CELL_EVALUATORS TO OLD__DW_SCAFFOLDING_CELL_EVALUATORS;
RENAME TABLE DW_SCAFFOLDING_CRITERIA TO OLD__DW_SCAFFOLDING_CRITERIA;
RENAME TABLE DW_SCAFFOLDING_LEVELS TO OLD__DW_SCAFFOLDING_LEVELS;
RENAME TABLE DW_SESSION TO OLD__DW_SESSION;
RENAME TABLE DW_SITE_USERS TO OLD__DW_SITE_USERS;
RENAME TABLE DW_SITES TO OLD__DW_SITES;
RENAME TABLE DW_TEMPLATE_FILE_REF TO OLD__DW_TEMPLATE_FILE_REF;
RENAME TABLE DW_USERS TO OLD__DW_USERS;
RENAME TABLE DW_WIZARD TO OLD__DW_WIZARD;
RENAME TABLE DW_WIZARD_CATEGORY TO OLD__DW_WIZARD_CATEGORY;
RENAME TABLE DW_WIZARD_COMPLETED TO OLD__DW_COMPLETED;
RENAME TABLE DW_WIZARD_COMPLETED_CATEGORY TO OLD__DW_WIZARD_COMPLETED_CATEGORY;
RENAME TABLE DW_WIZARD_COMPLETED_PAGE TO OLD__DW_COMPLETED_PAGE;
RENAME TABLE DW_WIZARD_PAGE TO OLD__DW_WIZARD_PAGE;
RENAME TABLE DW_WIZARD_PAGE_ATTACHMENTS TO OLD__DW_WIZARD_PAGE_ATTACHMENTS;
RENAME TABLE DW_WIZARD_PAGE_DEF TO OLD__DW_WIZARD_PAGE_DEF;
RENAME TABLE DW_WIZARD_PAGE_DEF_ADD_FORMS TO OLD__DW_WIZARD_PAGE_DEF_ADD_FORMS;
RENAME TABLE DW_WIZARD_PAGE_FORMS TO OLD__DW_WIZARD_PAGE_FORMS;
RENAME TABLE DW_WIZARD_PAGE_SEQUENCE TO OLD__DW_WIZARD_PAGE_SEQUENCE;
RENAME TABLE DW_WIZARD_STYLE TO OLD__DW_WIZARD_STYLE;
RENAME TABLE DW_WIZARD_SUPPORT_ITEM TO OLD__DW_SUPPORT_ITEM;
RENAME TABLE DW_WORKFLOW_PARENT TO OLD__DW_WORKFLOW_PARENT;

-- Rename the ELL_* tables
RENAME TABLE ELL_CONFIGURATION TO OLD__ELL_CONFIGURATION;
RENAME TABLE ELL_MEETING_ACCESS TO OLD__ELL_MEETING_ACCESS;
RENAME TABLE ELL_MEETING_ACCESS_COURSE TO OLD__ELL_MEETING_ACCESS_COURSE;
RENAME TABLE ELL_MEETING_ACCESS_COURSE_TMP TO OLD__ELL_MEETING_ACCESS_COURSE_TMP;
RENAME TABLE ELL_MEETING_ACCESS_PRIVATE TO OLD__ELL_MEETING_ACCESS_PRIVATE;
RENAME TABLE ELL_MEETING_ACCESS_PRIVATE_TMP TO OLD__ELL_MEETING_ACCESS_PRIVATE_TMP;
RENAME TABLE ELL_MEETING_ACCESS_SECTION TO OLD__ELL_MEETING_ACCESS_SECTION;
RENAME TABLE ELL_MEETING_ACCESS_SECTION_TMP TO OLD__ELL_MEETING_ACCESS_SECTION_TMP;
RENAME TABLE ELL_MEETING_ACCESS_TMP TO OLD__ELL_MEETING_ACCESS_TMP;
RENAME TABLE ELL_MEETING_METADATA TO OLD__ELL_MEETING_METADATA;
RENAME TABLE ELL_MEETING_METADATA_TMP TO OLD__ELL_MEETING_METADATA_TMP;
RENAME TABLE ELL_SESSION_DATA TO OLD__ELL_SESSION_DATA;
RENAME TABLE ELL_SESSION_DATA_TMP TO OLD__ELL_SESSION_DATA_TMP;

-- Rename the METAOBJ tables
RENAME TABLE METAOBJ_FORM_DEF TO OLD__METAOBJ_FORM_DEF;

-- Rename the OSP_* tables
RENAME TABLE OSP_AUTHZ_SIMPLE TO OLD__OSP_AUTHZ_SIMPLE;
RENAME TABLE OSP_COMPLETED_WIZ_CATEGORY TO OLD__OSP_COMPLETED_WIZ_CATEGORY;
RENAME TABLE OSP_COMPLETED_WIZARD TO OLD__OSP_COMPLETED_WIZARD;
RENAME TABLE OSP_COMPLETED_WIZARD_PAGE TO OLD__OSP_COMPLETED_WIZARD_PAGE;
RENAME TABLE OSP_GUIDANCE TO OLD__OSP_GUIDANCE;
RENAME TABLE OSP_GUIDANCE_ITEM TO OLD__OSP_GUIDANCE_ITEM;
RENAME TABLE OSP_GUIDANCE_ITEM_FILE TO OLD__OSP_GUIDANCE_ITEM_FILE;
RENAME TABLE OSP_HELP_GLOSSARY TO OLD__OSP_HELP_GLOSSARY;
RENAME TABLE OSP_HELP_GLOSSARY_DESC TO OLD__OSP_HELP_GLOSSARY_DESC;
RENAME TABLE OSP_LIST_CONFIG TO OLD__OSP_LIST_CONFIG;
RENAME TABLE OSP_MATRIX TO OLD__OSP_MATRIX;
RENAME TABLE OSP_MATRIX_CELL TO OLD__OSP_MATRIX_CELL;
RENAME TABLE OSP_MATRIX_LABEL TO OLD__OSP_MATRIX_LABEL;
RENAME TABLE OSP_PORTAL_CATEGORY_PAGES TO OLD__OSP_PORTAL_CATEGORY_PAGES;
RENAME TABLE OSP_PORTAL_SITE_TYPES TO OLD__OSP_PORTAL_SITE_TYPES;
RENAME TABLE OSP_PORTAL_SPECIAL_SITES TO OLD__OSP_PORTAL_SPECIAL_SITES;
RENAME TABLE OSP_PORTAL_TOOL_CATEGORY TO OLD__OSP_PORTAL_TOOL_CATEGORY;
RENAME TABLE OSP_PORTAL_TOOL_FUNCTIONS TO OLD__OSP_PORTAL_TOOL_FUNCTIONS;
RENAME TABLE OSP_PORTAL_TOOL_TYPE TO OLD__OSP_PORTAL_TOOL_TYPE;
RENAME TABLE OSP_PRES_ITEMDEF_MIMETYPE TO OLD__OSP_PRES_ITEMDEF_MIMETYPE;
RENAME TABLE OSP_PRESENTATION TO OLD__OSP_PRESENTATION;
RENAME TABLE OSP_PRESENTATION_COMMENT TO OLD__OSP_PRESENTATION_COMMENT;
RENAME TABLE OSP_PRESENTATION_ITEM TO OLD__OSP_PRESENTATION_ITEM;
RENAME TABLE OSP_PRESENTATION_ITEM_DEF TO OLD__OSP_PRESENTATION_ITEM_DEF;
RENAME TABLE OSP_PRESENTATION_ITEM_PROPERTY TO OLD__OSP_PRESENTATION_ITEM_PROPERTY;
RENAME TABLE OSP_PRESENTATION_LAYOUT TO OLD__OSP_PRESENTATION_LAYOUT;
RENAME TABLE OSP_PRESENTATION_LOG TO OLD__OSP_PRESENTATION_LOG;
RENAME TABLE OSP_PRESENTATION_PAGE TO OLD__OSP_PRESENTATION_PAGE;
RENAME TABLE OSP_PRESENTATION_PAGE_ITEM TO OLD__OSP_PRESENTATION_PAGE_ITEM;
RENAME TABLE OSP_PRESENTATION_PAGE_REGION TO OLD__OSP_PRESENTATION_PAGE_REGION;
RENAME TABLE OSP_PRESENTATION_TEMPLATE TO OLD__OSP_PRESENTATION_TEMPLATE;
RENAME TABLE OSP_REVIEW TO OLD__OSP_REVIEW;
RENAME TABLE OSP_SCAFFOLDING TO OLD__OSP_SCAFFOLDING;
RENAME TABLE OSP_SCAFFOLDING_ATTACHMENTS TO OLD__OSP_SCAFFOLDING_ATTACHMENTS;
RENAME TABLE OSP_SCAFFOLDING_CELL TO OLD__OSP_SCAFFOLDING_CELL;
RENAME TABLE OSP_SCAFFOLDING_CELL_FORM_DEFS TO OLD__OSP_SCAFFOLDING_CELL_FORM_DEFS;
RENAME TABLE OSP_SCAFFOLDING_CRITERIA TO OLD__OSP_SCAFFOLDING_CRITERIA;
RENAME TABLE OSP_SCAFFOLDING_FORM_DEFS TO OLD__OSP_SCAFFOLDING_FORM_DEFS;
RENAME TABLE OSP_SCAFFOLDING_LEVELS TO OLD__OSP_SCAFFOLDING_LEVELS;
RENAME TABLE OSP_SITE_TOOL TO OLD__OSP_SITE_TOOL;
RENAME TABLE OSP_STYLE TO OLD__OSP_STYLE;
RENAME TABLE OSP_TEMPLATE_FILE_REF TO OLD__OSP_TEMPLATE_FILE_REF;
RENAME TABLE OSP_WIZ_PAGE_ATTACHMENT TO OLD__OSP_WIZ_PAGE_ATTACHMENT;
RENAME TABLE OSP_WIZ_PAGE_DEF_ATTACHMENTS TO OLD__OSP_WIZ_PAGE_DEF_ATTACHMENTS;
RENAME TABLE OSP_WIZ_PAGE_FORM TO OLD__OSP_WIZ_PAGE_FORM;
RENAME TABLE OSP_WIZARD TO OLD__OSP_WIZARD;
RENAME TABLE OSP_WIZARD_CATEGORY TO OLD__OSP_WIZARD_CATEGORY;
RENAME TABLE OSP_WIZARD_PAGE TO OLD__OSP_WIZARD_PAGE;
RENAME TABLE OSP_WIZARD_PAGE_DEF TO OLD__OSP_WIZARD_PAGE_DEF;
RENAME TABLE OSP_WIZARD_PAGE_SEQUENCE TO OLD__OSP_WIZARD_PAGE_SEQUENCE;
RENAME TABLE OSP_WORKFLOW TO OLD__OSP_WORKFLOW;
RENAME TABLE OSP_WORKFLOW_ITEM TO OLD__OSP_WORKFLOW_ITEM;
RENAME TABLE OSP_WORKFLOW_PARENT TO OLD__OSP_WORKFLOW_PARENT;
-- END OWL-2663

-- SAK-31840 drop defaults as its now managed in the POJO
alter table GB_GRADABLE_OBJECT_T alter column IS_EXTRA_CREDIT drop default;
alter table GB_GRADABLE_OBJECT_T alter column HIDE_IN_ALL_GRADES_TABLE drop default;
-- END SAK-31840
