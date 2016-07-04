alter table lesson_builder_items modify html mediumtext; 
alter table lesson_builder_items modify ownerGroups text;
alter table lesson_builder_items modify gradebookId varchar(100);
alter table lesson_builder_items modify altGradebook varchar(100);

alter table lesson_builder_student_pages modify owner varchar(99);
alter table lesson_builder_student_pages modify groupid varchar(99);
alter table lesson_builder_groups modify groupId varchar(99);
alter table lesson_builder_groups modify siteId varchar(99);
alter table lesson_builder_pages modify toolId varchar (99);
alter table lesson_builder_pages modify siteId varchar (99);
alter table lesson_builder_pages modify owner varchar (99);
alter table lesson_builder_pages modify groupid varchar (99);
alter table lesson_builder_comments modify author varchar (99);
alter table lesson_builder_log modify userId varchar (99);
alter table lesson_builder_log modify toolId varchar (99);

create index lesson_builder_qr_questionId_userId on lesson_builder_q_responses(questionId, userId);
create index lesson_builder_qr_total_qi on lesson_builder_qr_totals(questionId);
