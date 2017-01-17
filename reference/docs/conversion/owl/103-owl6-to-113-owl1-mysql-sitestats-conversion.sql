-- OWL 10.3-owl6 -> 11.3-owl1 MySQL conversion script for external SiteStats database
-- ----------------------------------------------------------------------------------

-- 10.3 -> 11.0 -----------------------------------------------------------

-- SAK-29546 Add site visit totals per user
CREATE TABLE SST_PRESENCE_TOTALS (
                ID int(20) NOT NULL auto_increment,
                SITE_ID varchar(99) NOT NULL,
                USER_ID varchar(99) NOT NULL,
                TOTAL_VISITS int NOT NULL,
                LAST_VISIT_TIME datetime NOT NULL,
                UNIQUE KEY(SITE_ID, USER_ID),
                PRIMARY KEY(ID));
-- END SAK-29546

CREATE TABLE SST_LESSONBUILDER (
  ID bigint(20) NOT NULL AUTO_INCREMENT,
  USER_ID varchar(99) NOT NULL,
  SITE_ID varchar(99) NOT NULL,
  PAGE_REF varchar(255) NOT NULL,
  PAGE_ID bigint(20) NOT NULL,
  PAGE_ACTION varchar(12) NOT NULL,
  PAGE_DATE date NOT NULL,
  PAGE_COUNT bigint(20) NOT NULL,
  PRIMARY KEY (ID),
  KEY SST_LESSONBUILDER_PAGE_ACT_IDX (PAGE_ACTION),
  KEY SST_LESSONBUILDER_DATE_IX (PAGE_DATE),
  KEY SST_LESSONBUILDER_SITE_ID_IX (SITE_ID),
  KEY SST_LESSONBUILDER_USER_ID_IX (USER_ID)
);

-- 11.1 -> 11.2 ----------------------------------------------------------

-- SAK-31276 remove unncecessary keys because there is a composite key that handles this
DROP INDEX SST_PRESENCE_SITE_ID_IX ON SST_PRESENCES;
DROP INDEX SST_EVENTS_USER_ID_IX ON SST_EVENTS;
-- END SAK-31276

