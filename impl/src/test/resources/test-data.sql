INSERT INTO course_group(id, dept, title) VALUES('course-1', '3C05', 'The Politics of Brazil' );
INSERT INTO course_group(id, dept, title) VALUES('course-2', '3C05', 'The Politics of Mexico' );
INSERT INTO course_group(id, dept, title) VALUES('course-3', '3C05', 'Testing of Open' );


INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-1', 'Lecture on Politics of Brazil', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 40, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-1');
INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-2', 'Lecture on Politics of Mexico', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 15, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-2');
INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-3', 'Seminar on South American Politics', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 15, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-3');

INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-4', 'Lecture on Politics of Brazil', '2011-HIL', DATE_SUB('2011-10-10', INTERVAL 3 WEEK), DATE_SUB('2011-10-10', INTERVAL 1 WEEK), 40, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-1');
INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-5', 'Seminar on South American Politics', '2011-HIL', DATE_SUB('2011-10-10', INTERVAL 3 WEEK), DATE_SUB('2011-10-10', INTERVAL 1 WEEK), 45, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-3');
INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-6', 'Lecture on Politics of Brazil', '2009-HIL', DATE_SUB('2009-10-10', INTERVAL 3 WEEK), DATE_SUB('2009-10-10', INTERVAL 1 WEEK), 40, 1, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-1');
INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-7', 'Seminar on South American Politics', '2009-HIL', DATE_SUB('2009-10-10', INTERVAL 3 WEEK), DATE_SUB('2009-10-10', INTERVAL 1 WEEK), 45, 1, 'c10cdf4b-7c10-423c-8319-2d477051a94e', 'tc-3');

INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-8', 'Seminar on South American Politics', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 5, 5, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-3');

INSERT INTO course_component(id, title, termcode, opens, closes, size, taken, administrator, componentId) VALUES('comp-9', 'Component Type', 'NOW', DATE_SUB(NOW(), INTERVAL 3 WEEK), DATE_ADD(NOW(), INTERVAL 1 WEEK), 5, 4, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-4');

INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-1');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-3');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-4');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-5');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-6');
INSERT INTO course_group_component(course_group, component) VALUES('course-1', 'comp-7');

INSERT INTO course_group_component(course_group, component) VALUES('course-2', 'comp-2');
INSERT INTO course_group_component(course_group, component) VALUES('course-2', 'comp-3');

INSERT INTO course_group_component(course_group, component) VALUES('course-3', 'comp-9');

INSERT INTO course_prop(id,name,value) VALUES ('course-1', 'desc', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus tortor sapien, vestibulum non vestibulum eget, scelerisque quis enim. Donec congue sollicitudin magna, sagittis facilisis metus commodo sit amet. Fusce egestas, dolor ac suscipit condimentum, ipsum lacus iaculis mi, non facilisis felis tortor blandit libero. In euismod lorem ac dolor fringilla viverra. Maecenas varius viverra pretium. Ut eu massa neque. Aliquam erat volutpat. Morbi eget metus ac sem accumsan mattis vitae ac dolor. Praesent sed pellentesque dui. Praesent non faucibus nisl. Vestibulum purus purus, porttitor et sodales eu, sollicitudin at velit. Suspendisse potenti.');
INSERT INTO course_prop(id,name,value) VALUES ('course-2', 'desc', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus tortor sapien, vestibulum non vestibulum eget, scelerisque quis enim. Donec congue sollicitudin magna, sagittis facilisis metus commodo sit amet. Fusce egestas, dolor ac suscipit condimentum, ipsum lacus iaculis mi, non facilisis felis tortor blandit libero. In euismod lorem ac dolor fringilla viverra. Maecenas varius viverra pretium. Ut eu massa neque. Aliquam erat volutpat. Morbi eget metus ac sem accumsan mattis vitae ac dolor. Praesent sed pellentesque dui. Praesent non faucibus nisl. Vestibulum purus purus, porttitor et sodales eu, sollicitudin at velit. Suspendisse potenti.');
INSERT INTO course_prop(id,name,value) VALUES ('course-3', 'desc', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus tortor sapien, vestibulum non vestibulum eget, scelerisque quis enim. Donec congue sollicitudin magna, sagittis facilisis metus commodo sit amet. Fusce egestas, dolor ac suscipit condimentum, ipsum lacus iaculis mi, non facilisis felis tortor blandit libero. In euismod lorem ac dolor fringilla viverra. Maecenas varius viverra pretium. Ut eu massa neque. Aliquam erat volutpat. Morbi eget metus ac sem accumsan mattis vitae ac dolor. Praesent sed pellentesque dui. Praesent non faucibus nisl. Vestibulum purus purus, porttitor et sodales eu, sollicitudin at velit. Suspendisse potenti.');

INSERT INTO course_prop(id, name, value) VALUES ('comp-1','teacher.email', 'some.body@dep.ox.ac.uk');
INSERT INTO course_prop(id, name, value) VALUES ('comp-1','teacher.name', 'Some Body');

INSERT INTO course_prop(id, name, value) VALUES ('comp-2','teacher.email', 'some.body@dep.ox.ac.uk');
INSERT INTO course_prop(id, name, value) VALUES ('comp-2','teacher.name', 'Some Body');

INSERT INTO course_prop(id, name, value) VALUES ('comp-2', 'sessions', '8');
INSERT INTO course_prop(id, name, value) VALUES ('comp-2', 'slot', 'Monday 1pm (1 hour)');
INSERT INTO course_prop(id, name, value) VALUES ('comp-2', 'when', 'Michaelmas 2010');

INSERT INTO course_prop(id, name, value) VALUES ('comp-9','teacher.email', 'some.body@dep.ox.ac.uk');
INSERT INTO course_prop(id, name, value) VALUES ('comp-9','teacher.name', 'Some Body');

INSERT INTO course_prop(id, name, value) VALUES ('comp-9', 'sessions', '8');
INSERT INTO course_prop(id, name, value) VALUES ('comp-9', 'slot', 'Monday 1pm (1 hour)');
INSERT INTO course_prop(id, name, value) VALUES ('comp-9', 'when', 'Michaelmas 2010');

INSERT INTO course_signup(id, userId, status, supervisorId) VALUES ('signup1', 'current', 'ACCEPTED', '1');

INSERT INTO course_component_signup(signup, component) VALUES ('signup1', 'comp-6');
INSERT INTO course_component_signup(signup, component) VALUES ('signup1', 'comp-7');