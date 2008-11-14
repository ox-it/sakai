--*********************************************************************************
-- $URL$
-- $Id$
--**********************************************************************************
--
-- Copyright (c) 2008 Etudes, Inc.
-- 
-- Portions completed before September 1, 2008
-- Copyright (c) 2007, 2008 The Regents of the University of Michigan & Foothill College, ETUDES Project
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
-- Drops all Mneme Tables
-----------------------------------------------------------------------------

DROP TABLE MNEME_ANSWER;
DROP TABLE MNEME_SUBMISSION;
DROP SEQUENCE MNEME_SUBMISSION_SEQ;
DROP SEQUENCE MNEME_ANSWER_SEQ;

DROP TABLE MNEME_ASSESSMENT_PART_DETAIL;
DROP TABLE MNEME_ASSESSMENT_PART;
DROP TABLE MNEME_ASSESSMENT_ACCESS;
DROP TABLE MNEME_ASSESSMENT;
DROP SEQUENCE MNEME_ASSESSMENT_SEQ;
DROP SEQUENCE MNEME_ASSESSMENT_ACCESS_SEQ;
DROP SEQUENCE MNEME_ASSESSMENT_PART_SEQ;

DROP TABLE MNEME_POOL;
DROP SEQUENCE MNEME_POOL_SEQ;

DROP TABLE MNEME_QUESTION;
DROP SEQUENCE MNEME_QUESTION_SEQ;

