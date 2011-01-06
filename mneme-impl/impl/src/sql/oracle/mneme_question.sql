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
-- Mneme Question DDL
-----------------------------------------------------------------------------

CREATE TABLE MNEME_QUESTION
(
	CONTEXT				VARCHAR2 (99),
	CREATED_BY_DATE		NUMBER,
	CREATED_BY_USER		VARCHAR2 (99),
	DESCRIPTION			VARCHAR2 (255 CHAR),
	EXPLAIN_REASON		CHAR (1),
	FEEDBACK			CLOB,
	HINTS				CLOB,
	HISTORICAL			CHAR (1),
	ID					NUMBER NOT NULL PRIMARY KEY,
	MINT				CHAR (1),
	MODIFIED_BY_DATE	NUMBER,
	MODIFIED_BY_USER	VARCHAR2 (99),
	POOL_ID				NUMBER,
	PRESENTATION_TEXT	CLOB,
	PRESENTATION_ATTACHMENTS  CLOB,
	SURVEY				CHAR (1),
	TYPE				VARCHAR2 (99),
	VALID				CHAR (1),
	GUEST				CLOB
);

CREATE SEQUENCE MNEME_QUESTION_SEQ;

CREATE INDEX MNEME_QUESTION_IDX_MHPSV ON MNEME_QUESTION
(
	MINT		ASC,
	HISTORICAL	ASC,
	POOL_ID		ASC,
	SURVEY		ASC,
	VALID		ASC
);
