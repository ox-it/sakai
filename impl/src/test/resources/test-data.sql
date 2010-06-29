INSERT INTO course_group VALUES('course-1', '3C05', 'The Politics of Brazil' );
INSERT INTO course_group VALUES('course-2', '3C05', 'The Politics of Mexico' );

INSERT INTO course_component VALUES('comp-1', 'Lecture on Politics of Brazil', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 40, 0, 'tc-1');
INSERT INTO course_component VALUES('comp-2', 'Lecture on Politics of Mexico', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 45, 0, 'tc-2');
INSERT INTO course_component VALUES('comp-3', 'Seminar on South American Politics', '2010-HIL', DATE_SUB('2010-10-10', INTERVAL 3 WEEK), DATE_SUB('2010-10-10', INTERVAL 1 WEEK), 45, 0, 'tc-3');

INSERT INTO course_component VALUES('comp-4', 'Lecture on Politics of Brazil', '2011-HIL', DATE_SUB('2011-10-10', INTERVAL 3 WEEK), DATE_SUB('2011-10-10', INTERVAL 1 WEEK), 40, 0, 'tc-1');
INSERT INTO course_component VALUES('comp-5', 'Seminar on South American Politics', '2011-HIL', DATE_SUB('2011-10-10', INTERVAL 3 WEEK), DATE_SUB('2011-10-10', INTERVAL 1 WEEK), 45, 0, 'tc-3');

INSERT INTO course_component VALUES('comp-6', 'Lecture on Politics of Brazil', '2009-HIL', DATE_SUB('2009-10-10', INTERVAL 3 WEEK), DATE_SUB('2009-10-10', INTERVAL 1 WEEK), 40, 0, 'tc-1');
INSERT INTO course_component VALUES('comp-7', 'Seminar on South American Politics', '2009-HIL', DATE_SUB('2009-10-10', INTERVAL 3 WEEK), DATE_SUB('2009-10-10', INTERVAL 1 WEEK), 45, 0, 'tc-3');

INSERT INTO course_group_component VALUES('course-1', 'comp-1');
INSERT INTO course_group_component VALUES('course-1', 'comp-3');
INSERT INTO course_group_component VALUES('course-2', 'comp-1');
INSERT INTO course_group_component VALUES('course-2', 'comp-3');
INSERT INTO course_group_component VALUES('course-1', 'comp-4');
INSERT INTO course_group_component VALUES('course-1', 'comp-5');