--*********************************************************************************
-- $URL$
-- $Id$
--**********************************************************************************
--
-- Copyright (c) 2008, 2009, 2010 Etudes, Inc.
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

CREATE INDEX MNEME_ASSESSMENT_PART_DETAIL_IDX_QID ON MNEME_ASSESSMENT_PART_DETAIL
(
	QUESTION_ID	ASC
);

CREATE INDEX MNEME_ASSESSMENT_PART_DETAIL_IDX_POOLID ON MNEME_ASSESSMENT_PART_DETAIL
(
	POOL_ID	ASC
);

-- Note: this will populate the new ID field sequentially across the current set of detail records
ALTER TABLE MNEME_ASSESSMENT_PART_DETAIL ADD (ID BIGINT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY, SEQ INT UNSIGNED, POINTS FLOAT);

ALTER TABLE MNEME_ASSESSMENT ADD (POOL BIGINT UNSIGNED, NEEDSPOINTS CHAR(1));

UPDATE MNEME_ASSESSMENT_PART_DETAIL SET SEQ=NUM_QUESTIONS_SEQ;
