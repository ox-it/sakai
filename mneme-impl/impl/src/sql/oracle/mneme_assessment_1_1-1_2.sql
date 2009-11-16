--*********************************************************************************
-- $URL$
-- $Id$
--**********************************************************************************
--
-- Copyright (c) 2008, 2009 Etudes, Inc.
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

-- TODO:add to MNEME_ASSESSMENT_PART_DETAIL:	ID						NUMBER NOT NULL PRIMARY KEY
-- TODO: add to MNEME_ASSESSMENT_PART_DETAIL:	SEQ						NUMBER

-- then populate ID from the above sequence...

UPDATE MNEME_ASSESSMENT_PART_DETAIL SET SEQ=NUM_QUESTIONS_SEQ;

-- TODO: add to MNEME_ASSESSMENT POOL BIGINT UNSIGNED NULL
