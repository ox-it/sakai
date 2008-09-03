--*********************************************************************************
-- $URL$
-- $Id$
--**********************************************************************************
--
-- Copyright (c) 2008 Etudes, Inc.
-- 
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Portions completed before September 1, 2008 Copyright (c) 2007, 2008 Sakai Foundation,
-- licensed under the Educational Community License, Version 2.0
--
--       http://www.osedu.org/licenses/ECL-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
--*********************************************************************************/

-----------------------------------------------------------------------------
-- Mneme Question DDL changes between 1.0 and 1.1
-----------------------------------------------------------------------------

ALTER TABLE MNEME_QUESTION
	ADD (SURVEY CHAR (1)),
	ADD (VALID CHAR (1));

DROP INDEX MNEME_QUESTION_IDX_MHP ON MNEME_QUESTION;

UPDATE MNEME_QUESTION SET SURVEY='0', VALID='1';
UPDATE MNEME_QUESTION SET SURVEY='1' WHERE TYPE='mneme:LikertScale' AND HISTORICAL='0';

CREATE INDEX MNEME_QUESTION_IDX_MHPSV ON MNEME_QUESTION
(
	MINT		ASC,
	HISTORICAL	ASC,
	POOL_ID		ASC,
	SURVEY		ASC,
	VALID		ASC
);

ALTER TABLE MNEME_QUESTION
	CHANGE FEEDBACK FEEDBACK LONGTEXT,
	CHANGE HINTS HINTS LONGTEXT,
	CHANGE PRESENTATION_TEXT PRESENTATION_TEXT LONGTEXT,
	CHANGE GUEST GUEST LONGTEXT;
