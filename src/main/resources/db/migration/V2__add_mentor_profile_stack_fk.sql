DO $$
DECLARE
    row_count INTEGER;
BEGIN
    UPDATE mentor_profiles
    SET stack_id = (SELECT id FROM stacks ORDER BY id LIMIT 1)
    WHERE stack_id IS NULL;
    
    GET DIAGNOSTICS row_count = ROW_COUNT;
    
    IF row_count <> 1 THEN
        RAISE EXCEPTION 'Expected exactly 1 row to be updated, but got %', row_count;
    END IF;
END $$;

ALTER TABLE mentor_profiles
    ADD CONSTRAINT fk_mentor_profiles_stack FOREIGN KEY (stack_id) REFERENCES stacks(id);

ALTER TABLE mentor_profiles
    ALTER COLUMN stack_id SET NOT NULL;
