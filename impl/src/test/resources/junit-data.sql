INSERT INTO course_group VALUES('course-1', '3C05', 'The Politics of Brazil' );
INSERT INTO course_group VALUES('course-2', '3C05', 'The Politics of Mexico' );


INSERT INTO course_component VALUES('comp-1', 'Lecture on Politics of Brazil', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 40, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-1');
INSERT INTO course_component VALUES('comp-2', 'Lecture on Politics of Mexico', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 45, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-2');
INSERT INTO course_component VALUES('comp-3', 'Seminar on South American Politics', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 45, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-3');

INSERT INTO course_component VALUES('comp-4', 'Lecture on Politics of Brazil', '2011-HIL', DATE_SUB('2011-10-10', INTERVAL 3 WEEK), DATE_SUB('2011-10-10', INTERVAL 1 WEEK), 40, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-1');
INSERT INTO course_component VALUES('comp-5', 'Seminar on South American Politics', '2011-HIL', DATE_SUB('2011-10-10', INTERVAL 3 WEEK), DATE_SUB('2011-10-10', INTERVAL 1 WEEK), 45, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-3');

INSERT INTO course_component VALUES('comp-6', 'Lecture on Politics of Brazil', '2009-HIL', DATE_SUB('2009-10-10', INTERVAL 3 WEEK), DATE_SUB('2009-10-10', INTERVAL 1 WEEK), 40, 0, 'd86d9720-eba4-40eb-bda3-91b3145729da', 'tc-1');
INSERT INTO course_component VALUES('comp-7', 'Seminar on South American Politics', '2009-HIL', DATE_SUB('2009-10-10', INTERVAL 3 WEEK), DATE_SUB('2009-10-10', INTERVAL 1 WEEK), 45, 0, 'c10cdf4b-7c10-423c-8319-2d477051a94e', 'tc-3');

INSERT INTO course_group_component VALUES('course-1', 'comp-1');
INSERT INTO course_group_component VALUES('course-1', 'comp-3');
INSERT INTO course_group_component VALUES('course-2', 'comp-1');
INSERT INTO course_group_component VALUES('course-2', 'comp-3');
INSERT INTO course_group_component VALUES('course-1', 'comp-4');
INSERT INTO course_group_component VALUES('course-1', 'comp-5');


INSERT INTO course_prop(id,name,value) VALUES ('course-1', 'desc', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus tortor sapien, vestibulum non vestibulum eget, scelerisque quis enim. Donec congue sollicitudin magna, sagittis facilisis metus commodo sit amet. Fusce egestas, dolor ac suscipit condimentum, ipsum lacus iaculis mi, non facilisis felis tortor blandit libero. In euismod lorem ac dolor fringilla viverra. Maecenas varius viverra pretium. Ut eu massa neque. Aliquam erat volutpat. Morbi eget metus ac sem accumsan mattis vitae ac dolor. Praesent sed pellentesque dui. Praesent non faucibus nisl. Vestibulum purus purus, porttitor et sodales eu, sollicitudin at velit. Suspendisse potenti.');
INSERT INTO course_prop(id,name,value) VALUES ('course-2', 'desc', 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus tortor sapien, vestibulum non vestibulum eget, scelerisque quis enim. Donec congue sollicitudin magna, sagittis facilisis metus commodo sit amet. Fusce egestas, dolor ac suscipit condimentum, ipsum lacus iaculis mi, non facilisis felis tortor blandit libero. In euismod lorem ac dolor fringilla viverra. Maecenas varius viverra pretium. Ut eu massa neque. Aliquam erat volutpat. Morbi eget metus ac sem accumsan mattis vitae ac dolor. Praesent sed pellentesque dui. Praesent non faucibus nisl. Vestibulum purus purus, porttitor et sodales eu, sollicitudin at velit. Suspendisse potenti.');

INSERT INTO course_prop(id, name, value) VALUES ('comp-1','teacher.email', 'some.body@dep.ox.ac.uk');
INSERT INTO course_prop(id, name, value) VALUES ('comp-1','teacher.name', 'some.body@dep.ox.ac.uk');

INSERT INTO course_prop(id, name, value) VALUES ('comp-2','teacher.email', 'some.body@dep.ox.ac.uk');
INSERT INTO course_prop(id, name, value) VALUES ('comp-2','teacher.name', 'some.body@dep.ox.ac.uk');