-- Drop foreign keys referencing domain tables first
ALTER TABLE IF EXISTS review_sessions DROP CONSTRAINT IF EXISTS fk2h7hk1sbtq29f6vo3e9xxxb61; -- review_sessions -> domain_mentor_profiles
ALTER TABLE IF EXISTS review_sessions DROP CONSTRAINT IF EXISTS fk3xi400cxto1wmgdyp99t8kdj0; -- review_sessions -> domain_student_profiles
ALTER TABLE IF EXISTS mentor_availability DROP CONSTRAINT IF EXISTS fkc399yk4ai5a48f42bidslwvxu; -- mentor_availability -> domain_mentor_profiles

-- Drop the tables
DROP TABLE IF EXISTS domain_mentor_profiles CASCADE;
DROP TABLE IF EXISTS domain_student_profiles CASCADE;
DROP TABLE IF EXISTS domain_users CASCADE;

-- Re-add foreign keys to point to model tables (mentor_profiles and student_profiles)
ALTER TABLE ONLY review_sessions
    ADD CONSTRAINT fk_review_sessions_mentor FOREIGN KEY (mentor_id) REFERENCES mentor_profiles(id);

ALTER TABLE ONLY review_sessions
    ADD CONSTRAINT fk_review_sessions_student FOREIGN KEY (student_id) REFERENCES student_profiles(id);

ALTER TABLE ONLY mentor_availability
    ADD CONSTRAINT fk_mentor_availability_mentor FOREIGN KEY (mentor_id) REFERENCES mentor_profiles(id);
