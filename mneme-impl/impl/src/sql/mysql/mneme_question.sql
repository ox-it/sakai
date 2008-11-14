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
	CONTEXT				VARCHAR (99),
	CREATED_BY_DATE		BIGINT,
	CREATED_BY_USER		VARCHAR (99),
	DESCRIPTION			VARCHAR (255),
	EXPLAIN_REASON		CHAR (1),
	FEEDBACK			LONGTEXT,
	HINTS				LONGTEXT,
	HISTORICAL			CHAR (1),
	ID					BIGINT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
	MINT				CHAR (1),
	MODIFIED_BY_DATE	BIGINT,
	MODIFIED_BY_USER	VARCHAR (99),
	POOL_ID				BIGINT UNSIGNED,
	PRESENTATION_TEXT	LONGTEXT,
	SURVEY				CHAR (1),
	TYPE				VARCHAR (99),
	VALID				CHAR (1),
	GUEST				LONGTEXT
);

CREATE INDEX MNEME_QUESTION_IDX_MHPSV ON MNEME_QUESTION
(
	MINT		ASC,
	HISTORICAL	ASC,
	POOL_ID		ASC,
	SURVEY		ASC,
	VALID		ASC
);

