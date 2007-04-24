--

CREATE TABLE hierarchy_nodes (
  id varchar(36) NOT NULL,
  pathhash varchar(64) NOT NULL,
  path VARCHAR,
  parent_id varchar(36) default NULL,
  realm VARCHAR,
  version datetime NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (pathhash)
); 

CREATE TABLE hierarchy_property (
  id varchar(36) NOT NULL,
  name varchar(64) default '' NOT NULL,
  propvalue varchar(254) default '' NOT NULL,
  node_id varchar(36) NOT NULL,
  version datetime NOT NULL,
  PRIMARY KEY  (id),
  UNIQUE (node_id,name)
); 
