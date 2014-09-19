--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/mneme/branches/nextrls/mneme-impl/impl/src/sql/mysql/mneme_assessment_2.1.14_2.1.15.sql $
-- $Id: mneme_assessment_2.1.14_2.1.15.sql 3673 2012-12-03 23:48:35Z ggolden $
--**********************************************************************************
--
-- Copyright (c) 2014 Etudes, Inc.
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
--*********************************************************************************/

-----------------------------------------------------------------------------
-- Mneme Assessment DDL changes between 2.1.34 and 2.1.35
-----------------------------------------------------------------------------

ALTER TABLE MNEME_ASSESSMENT ADD (NOTIFY_EVAL CHAR (1));
ALTER TABLE MNEME_ASSESSMENT ADD (EVAL_SENT BIGINT);
ALTER TABLE MNEME_ASSESSMENT ADD (HIDE_UNTIL_OPEN CHAR (1));
ALTER TABLE MNEME_ASSESSMENT_ACCESS ADD (HIDE_UNTIL_OPEN CHAR (1));
ALTER TABLE MNEME_ASSESSMENT_ACCESS ADD (OVERRIDE_HIDE_UNTIL_OPEN CHAR (1));
UPDATE MNEME_ASSESSMENT_ACCESS SET OVERRIDE_HIDE_UNTIL_OPEN='0';
UPDATE MNEME_ASSESSMENT_ACCESS SET HIDE_UNTIL_OPEN='0';
UPDATE MNEME_ASSESSMENT SET HIDE_UNTIL_OPEN='0';