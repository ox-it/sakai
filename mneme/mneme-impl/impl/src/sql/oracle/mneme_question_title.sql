--*********************************************************************************
-- $URL: https://source.etudes.org/svn/apps/mneme/branches/MN-745/mneme-impl/impl/src/sql/mysql/mneme_question_table.sql $
-- $Id: mneme_question_table.sql 9742 2015-01-07 19:56:05Z ggolden $
--**********************************************************************************
--
-- Copyright (c)  2015 Etudes, Inc.
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
-- Mneme Question Table DDL
-----------------------------------------------------------------------------

CREATE TABLE MNEME_QUESTION_TITLE 
(
  QUESTION_ID NUMBER NOT NULL PRIMARY KEY,
  TITLE VARCHAR2 (255)
);