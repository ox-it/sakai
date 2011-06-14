INSERT INTO course_group(id, dept, title) VALUES('course-1', '3C05', 'The Politics of Brazil' );
INSERT INTO course_group(id, dept, title) VALUES('course-2', '3C05', 'The Politics of Mexico' );
INSERT INTO course_group(id, dept, title) VALUES('course-3', '3C05', 'Testing of Open' );

INSERT INTO course_group_administrator(course_group, administrator) VALUES('course-1', 'admin' );
INSERT INTO course_group_administrator(course_group, administrator) VALUES('course-2', 'admin' );
INSERT INTO course_group_administrator(course_group, administrator) VALUES('course-3', 'admin1' );
INSERT INTO course_group_administrator(course_group, administrator) VALUES('course-3', 'admin2' );
INSERT INTO course_group_administrator(course_group, administrator) VALUES('course-3', 'admin3' );

INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-1', true, 'Lecture on Politics of Brazil', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 40, 0, 'tc-1');
INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-2', true, 'Lecture on Politics of Mexico', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 15, 0, 'tc-2');
INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-3', true, 'Seminar on South American Politics', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 15, 0, 'tc-3');
INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-4', true, 'Lecture on Politics of Brazil', '2011-HIL', DATE_SUB('2011-10-10', INTERVAL 3 WEEK), DATE_SUB('2011-10-10', INTERVAL 1 WEEK), 40, 0, 'tc-1');
INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-5', true, 'Seminar on South American Politics', '2011-HIL', DATE_SUB('2011-10-10', INTERVAL 3 WEEK), DATE_SUB('2011-10-10', INTERVAL 1 WEEK), 45, 0, 'tc-3');
INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-6', true, 'Lecture on Politics of Brazil', '2009-HIL', DATE_SUB('2009-10-10', INTERVAL 3 WEEK), DATE_SUB('2009-10-10', INTERVAL 1 WEEK), 40, 1, 'tc-1');
INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-7', true, 'Seminar on South American Politics', '2009-HIL', DATE_SUB('2009-10-10', INTERVAL 3 WEEK), DATE_SUB('2009-10-10', INTERVAL 1 WEEK), 45, 2, 'tc-3');
INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-8', true, 'Seminar on South American Politics', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 5, 5, 'tc-3');
INSERT INTO course_component(version, id, bookable, title, termcode, opens, closes, size, taken, componentId) VALUES(0, 'comp-9', true, 'Component Type', 'NOW', DATE_SUB(NOW(), INTERVAL 3 WEEK), DATE_ADD(NOW(), INTERVAL 1 WEEK), 5, 4, 'tc-4');

INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-1');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-3');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-4');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-5');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-6');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-7');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-8');

INSERT INTO course_group_component(course_group, component) VALUES('course-2', 'comp-2');
INSERT INTO course_group_component(course_group, component) VALUES('course-2', 'comp-3');

INSERT INTO course_group_component(course_group, component) VALUES('course-3', 'comp-9');

INSERT INTO course_signup(id, userId, status, supervisorId, groupId) VALUES ('signup1', 'current', 'ACCEPTED', '1', 'course-1');
INSERT INTO course_signup(id, userId, status, supervisorId, groupId) VALUES ('signup2', 'current', 'ACCEPTED', '1', 'course-1');

INSERT INTO course_component_signup(signup, component) VALUES ('signup1', 'comp-6');
INSERT INTO course_component_signup(signup, component) VALUES ('signup1', 'comp-7');

INSERT INTO course_component_signup(signup, component) VALUES ('signup2', 'comp-7');