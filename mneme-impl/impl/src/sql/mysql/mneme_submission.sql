--*********************************************************************************
-- $URL$
-- $Id$
--**********************************************************************************
--
-- Copyright (c) 2008, 2009, 2010, 2011 Etudes, Inc.
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
-- Mneme Submission DDL
-----------------------------------------------------------------------------

CREATE TABLE MNEME_SUBMISSION
(
	ASSESSMENT_ID		BIGINT UNSIGNED,
	COMPLETE			CHAR (1),
	CONTEXT				VARCHAR (99),
	EVAL_ATRIB_DATE		BIGINT,
	EVAL_ATRIB_USER		VARCHAR (99),
	EVAL_ATTACHMENTS	LONGTEXT,
	EVAL_COMMENT		LONGTEXT,
	EVAL_EVALUATED		CHAR (1),
	EVAL_SCORE			FLOAT,
	ID					BIGINT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
	RELEASED			CHAR,
	REVIEWED_DATE		BIGINT,
	START_DATE			BIGINT,
	SUBMITTED_DATE		BIGINT,
	TEST_DRIVE			CHAR,
	USERID				VARCHAR (99)
);

CREATE INDEX MNEME_SUBMISSION_IDX_ACT ON MNEME_SUBMISSION
(
	ASSESSMENT_ID		ASC,
	COMPLETE			ASC,
	TEST_DRIVE			ASC
);

CREATE INDEX MNEME_SUBMISSION_IDX_AUC ON MNEME_SUBMISSION
(
	ASSESSMENT_ID		ASC,
	USERID				ASC,
	COMPLETE			ASC
);

CREATE INDEX MNEME_SUBMISSION_IDX_CTU ON MNEME_SUBMISSION
(
	CONTEXT				ASC,
	TEST_DRIVE			ASC,
	USERID				ASC
);

CREATE INDEX MNEME_SUBMISSION_IDX_COMPLETE ON MNEME_SUBMISSION
(
	COMPLETE			ASC
);

-----------------------------------------------------------------------------

CREATE TABLE MNEME_ANSWER
(
	ANSWERED			CHAR (1),
	AUTO_SCORE			FLOAT,
	GUEST				LONGTEXT,
	EVAL_ATRIB_DATE		BIGINT,
	EVAL_ATRIB_USER		VARCHAR (99),
	EVAL_ATTACHMENTS	LONGTEXT,
	EVAL_COMMENT		LONGTEXT,
	EVAL_EVALUATED		CHAR (1),
	EVAL_SCORE			FLOAT,
	ID					BIGINT UNSIGNED AUTO_INCREMENT NOT NULL PRIMARY KEY,
	PART_ID				BIGINT UNSIGNED,
	QUESTION_ID			BIGINT UNSIGNED,
	QUESTION_TYPE		VARCHAR (99),
	REASON				LONGTEXT,
	REVIEW				CHAR (1),
	SUBMISSION_ID		BIGINT UNSIGNED,
	SUBMITTED_DATE		BIGINT
);

CREATE INDEX MNEME_ANSWER_IDX_QID ON MNEME_ANSWER
(
	QUESTION_ID			ASC
);

CREATE INDEX MNEME_ANSWER_IDX_SID ON MNEME_ANSWER
(
	SUBMISSION_ID		ASC
);
