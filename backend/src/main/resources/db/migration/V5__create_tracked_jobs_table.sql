CREATE TABLE tracked_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    search_profile_id BIGINT,
    company VARCHAR(500) NOT NULL,
    position_title VARCHAR(500) NOT NULL,
    location VARCHAR(500),
    source VARCHAR(100),
    job_url LONGTEXT,
    reference_id VARCHAR(500),
    match_score INT,
    status VARCHAR(50) NOT NULL,
    applied_date DATE,
    result_note VARCHAR(500),
    notes LONGTEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_tracked_jobs_profile
        FOREIGN KEY (search_profile_id)
        REFERENCES search_profiles(id)
        ON DELETE SET NULL,
    INDEX idx_tracked_jobs_profile_status (
        search_profile_id,
        status
    ),
    INDEX idx_tracked_jobs_company_position (
        company(191),
        position_title(191)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
