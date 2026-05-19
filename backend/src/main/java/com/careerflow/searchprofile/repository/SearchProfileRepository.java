package com.careerflow.searchprofile.repository;

import com.careerflow.searchprofile.entity.SearchProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchProfileRepository extends JpaRepository<SearchProfile, Long> {
}

