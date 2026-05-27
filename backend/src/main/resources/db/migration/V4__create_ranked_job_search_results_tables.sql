CREATE TABLE ranked_job_search_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    search_profile_id BIGINT NOT NULL,
    location VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ranked_job_search_runs_profile
        FOREIGN KEY (search_profile_id)
        REFERENCES search_profiles(id)
        ON DELETE CASCADE,
    INDEX idx_ranked_job_search_runs_profile_created_at (
        search_profile_id,
        created_at
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE ranked_job_search_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    search_run_id BIGINT NOT NULL,
    position_index INT NOT NULL,
    job_source VARCHAR(100),
    job_title VARCHAR(500),
    job_company VARCHAR(500),
    job_location VARCHAR(500),
    job_description LONGTEXT,
    job_url LONGTEXT,
    job_reference_id VARCHAR(500),
    job_published_at VARCHAR(100),
    full_description_available BOOLEAN NOT NULL DEFAULT FALSE,
    match_score INT,
    recommendation VARCHAR(100),
    match_summary LONGTEXT,
    matching_skills_json LONGTEXT,
    missing_skills_json LONGTEXT,
    concerns_json LONGTEXT,
    suggested_application_focus_json LONGTEXT,
    CONSTRAINT fk_ranked_job_search_results_run
        FOREIGN KEY (search_run_id)
        REFERENCES ranked_job_search_runs(id)
        ON DELETE CASCADE,
    INDEX idx_ranked_job_search_results_run_position (
        search_run_id,
        position_index
    ),
    INDEX idx_ranked_job_search_results_score (
        match_score
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
