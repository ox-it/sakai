-- OWL 10.3-owl6 -> 11.3-owl1 Oracle conversion script for external SiteStats database
-- ----------------------------------------------------------------------------------

-- 10.3 -> 11.0 -----------------------------------------------------------

-- SAK-29546 Add site visit totals per user
CREATE TABLE SST_PRESENCE_TOTALS (
                ID NUMBER(19,0) NOT NULL,
                SITE_ID varchar2(99) NOT NULL,
                USER_ID varchar2(99) NOT NULL,
                TOTAL_VISITS NUMBER(10,0) NOT NULL,
                LAST_VISIT_TIME DATE NOT NULL,
                UNIQUE KEY(SITE_ID, USER_ID),
                PRIMARY KEY(ID));

CREATE SEQUENCE SST_PRESENCE_TOTALS_ID START WITH 1 INCREMENT BY 1 nomaxvalue;
-- END SAK-29546

CREATE TABLE SST_LESSONBUILDER
(ID             NUMBER(19) PRIMARY KEY,
 USER_ID        VARCHAR2(99) NOT NULL,
 SITE_ID        VARCHAR2(99) NOT NULL,
 PAGE_REF       VARCHAR2(255) NOT NULL,
 PAGE_ID        NUMBER(19) NOT NULL,
 PAGE_ACTION    VARCHAR2(12) NOT NULL,
 PAGE_DATE      DATE NOT NULL,
 PAGE_COUNT     NUMBER(19) NOT NULL
);

CREATE SEQUENCE SST_LESSONBUILDER_ID;

CREATE INDEX SST_LESSONBUILDER_PAGE_ACT_IDX ON SST_LESSONBUILDER (PAGE_ACTION);

CREATE INDEX SST_LESSONBUILDER_DATE_IX ON SST_LESSONBUILDER (PAGE_DATE);

CREATE INDEX SST_LESSONBUILDER_SITE_ID_IX ON SST_LESSONBUILDER (SITE_ID);

CREATE INDEX SST_LESSONBUILDER_USER_ID_IX ON SST_LESSONBUILDER (USER_ID);

-- 11.1 -> 11.2 ----------------------------------------------------------

-- SAK-31276 remove unncecessary keys because there is a composite key that handles this
DROP INDEX SST_PRESENCE_SITE_ID_IX ON SST_PRESENCES;
DROP INDEX SST_EVENTS_USER_ID_IX ON SST_EVENTS;
-- END SAK-31276

