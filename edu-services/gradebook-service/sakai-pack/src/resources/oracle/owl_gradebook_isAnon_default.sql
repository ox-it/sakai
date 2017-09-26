-- On Oracle run this after auto.ddl creates the IS_ANON column:

UPDATE gb_gradable_object_t
SET is_anon = 0;
ALTER TABLE gb_gradable_object_t
MODIFY is_anon NOT NULL;
ALTER TABLE gb_gradable_object_t
MODIFY is_anon DEFAULT 0;
