-- ----------------------------------------------------------------------------------
-- OWL 10.3-owl6 -> 11.3-owl1 MySQL conversion script for external SiteStats database
-- ----------------------------------------------------------------------------------

alter table SST_PRESENCE_TOTALS modify column ID bigint(19) NOT NULL auto_increment;

alter table SST_DETAILED_EVENTS modify EVENT_REF varchar(512) not null;
