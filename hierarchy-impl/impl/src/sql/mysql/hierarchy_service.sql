--

CREATE TABLE `hierarchy_nodes` (
  `id` varchar(36) NOT NULL default '',
  `pathhash` varchar(64) NOT NULL,
  `path` text,
  `parent_id` varchar(36) default NULL,
  `realm` text,
  `version` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `pathhash` (`pathhash`)
); 

CREATE TABLE `hierarchy_property` (
  `id` varchar(36) NOT NULL default '',
  `name` varchar(64) NOT NULL default '',
  `propvalue` varchar(254) NOT NULL default '',
  `node_id` varchar(36) default NULL,
  `version` datetime NOT NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `node_id_name` (`node_id`,`name`)
); 

