-- Create search_profiles main table
CREATE TABLE search_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    summary LONGTEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create search_profile_search_roles table (ElementCollection)
CREATE TABLE search_profile_search_roles (
    search_profile_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT fk_search_roles FOREIGN KEY (search_profile_id) REFERENCES search_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create search_profile_alternative_career_roles table (ElementCollection)
CREATE TABLE search_profile_alternative_career_roles (
    search_profile_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT fk_alternative_roles FOREIGN KEY (search_profile_id) REFERENCES search_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create search_profile_keywords table (ElementCollection)
CREATE TABLE search_profile_keywords (
    search_profile_id BIGINT NOT NULL,
    keyword VARCHAR(255) NOT NULL,
    CONSTRAINT fk_keywords FOREIGN KEY (search_profile_id) REFERENCES search_profiles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

