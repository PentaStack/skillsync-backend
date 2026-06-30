DO $$
DECLARE
    default_stack_id BIGINT;
    unfilled_count INTEGER;
BEGIN
    -- Backfill any existing mentor_profiles that have no stack assigned.
    SELECT id INTO default_stack_id FROM stacks ORDER BY id LIMIT 1;

    UPDATE mentor_profiles
    SET stack_id = default_stack_id
    WHERE stack_id IS NULL;

    -- If profiles still lack a stack (e.g. no stacks exist to backfill from),
    -- the NOT NULL constraint below would fail; surface a clear error instead.
    SELECT COUNT(*) INTO unfilled_count
    FROM mentor_profiles
    WHERE stack_id IS NULL;

    IF unfilled_count > 0 THEN
        RAISE EXCEPTION 'Cannot set stack_id NOT NULL: % mentor_profiles row(s) have no stack and no stacks exist to backfill from', unfilled_count;
    END IF;
END $$;

ALTER TABLE mentor_profiles
    ADD CONSTRAINT fk_mentor_profiles_stack FOREIGN KEY (stack_id) REFERENCES stacks(id);

ALTER TABLE mentor_profiles
    ALTER COLUMN stack_id SET NOT NULL;
