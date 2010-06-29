INSERT INTO course_group VALUES('course-1', 'The Politics of Brazil' );
INSERT INTO course_group VALUES('course-2', 'The Politics of Mexico' );

INSERT INTO course_component VALUES('comp-1', 'Lecture on Politics of Brazil', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 40, 'tc-1');
INSERT INTO course_component VALUES('comp-2', 'Lecture on Politics of Mexico', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 45, 'tc-2');
INSERT INTO course_component VALUES('comp-3', 'Seminar on South American Politics', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 45, 'tc-3');

INSERT INTO course_group_component VALUES('course-1', 'comp-1');
INSERT INTO course_group_component VALUES('course-1', 'comp-3');
INSERT INTO course_group_component VALUES('course-2', 'comp-1');
INSERT INTO course_group_component VALUES('course-2', 'comp-3');
