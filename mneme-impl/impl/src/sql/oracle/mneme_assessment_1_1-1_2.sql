--*********************************************************************************
-- $URL$
-- $Id$
--**********************************************************************************
--
-- Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
-- Mneme Assessment DDL changes between 1.1 and 1.2
-----------------------------------------------------------------------------

CREATE SEQUENCE MNEME_ASSESSMENT_DETAIL_SEQ;
ALTER TABLE MNEME_ASSESSMENT_PART_DETAIL ADD (ID NUMBER PRIMARY KEY, SEQ NUMBER, POINTS FLOAT);

-- Populate the new ID field sequentially across the current set of detail records from the sequence
UPDATE MNEME_ASSESSMENT_PART_DETAIL SET ID=MNEME_ASSESSMENT_DETAIL_SEQ.NEXT;
ALTER TABLE MNEME_ASSESSMENT_PART_DETAIL MODIFY (ID NUMBER NOT NULL PRIMARY KEY);

ALTER TABLE MNEME_ASSESSMENT ADD (POOL NUMBER, NEEDSPOINTS CHAR(1), SHOW_MODEL_ANSWER CHAR(1));

UPDATE MNEME_ASSESSMENT_PART_DETAIL SET SEQ=NUM_QUESTIONS_SEQ;

CREATE INDEX MNEME_APD_IDX_QID ON MNEME_ASSESSMENT_PART_DETAIL
(
	QUESTION_ID	ASC
);

CREATE INDEX MNEME_APD_IDX_POOLID ON MNEME_ASSESSMENT_PART_DETAIL
(
	POOL_ID	ASC
);