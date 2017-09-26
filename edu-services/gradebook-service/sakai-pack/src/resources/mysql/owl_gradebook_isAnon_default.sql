-- On MySQL you will need to run the following database script after auto.ddl creates the IS_ANON column:

UPDATE gb_gradable_object_t
SET is_anon = 0;
ALTER TABLE gb_gradable_object_t
MODIFY IS_ANON TINYINT(1) NOT NULL DEFAULT 0;
