CREATE TABLE search_profile_generated_queries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    search_profile_id BIGINT NOT NULL,
    query_type VARCHAR(50) NOT NULL,
    query_text VARCHAR(500) NOT NULL,
    position_index INT NOT NULL,
    CONSTRAINT fk_generated_queries_profile
        FOREIGN KEY (search_profile_id)
        REFERENCES search_profiles(id)
        ON DELETE CASCADE,
    INDEX idx_generated_queries_profile_type_position (
        search_profile_id,
        query_type,
        position_index
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
