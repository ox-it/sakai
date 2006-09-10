?
CREATE TABLE `hierarchy_nodes` (
  `id` varchar(36) NOT NULL default '',
  `nodeid` varchar(36) NOT NULL default '',
  `parentid` varchar(36) NOT NULL default '',
  `name` varchar(64) default NULL,
  `path` text,
  `parent_id` varchar(36) default NULL,
  `realm` text,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `nodeid` (`nodeid`),
  KEY `FK1265CC2754A9C437` (`parent_id`),
  CONSTRAINT `FK1265CC2754A9C437` FOREIGN KEY (`parent_id`) REFERENCES `hierarchy_nodes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

CREATE TABLE `hierarchy_property` (
  `id` varchar(36) NOT NULL default '',
  `nodeid` varchar(36) NOT NULL default '',
  `name` varchar(64) NOT NULL default '',
  `propvalue` varchar(254) NOT NULL default '',
  `node_id` varchar(36) default NULL,
  PRIMARY KEY  (`id`),
  UNIQUE KEY `nodeid` (`nodeid`),
  KEY `FK7619289F574AFFDF` (`node_id`),
  CONSTRAINT `FK7619289F574AFFDF` FOREIGN KEY (`node_id`) REFERENCES `hierarchy_nodes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 

