ALTER TABLE tracked_jobs
    DROP FOREIGN KEY fk_tracked_jobs_profile;

ALTER TABLE tracked_jobs
    ADD CONSTRAINT fk_tracked_jobs_profile
        FOREIGN KEY (search_profile_id)
        REFERENCES search_profiles(id)
        ON DELETE CASCADE;
