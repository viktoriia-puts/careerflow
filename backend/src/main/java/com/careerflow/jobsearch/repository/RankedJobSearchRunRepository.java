package com.careerflow.jobsearch.repository;

import com.careerflow.jobsearch.entity.RankedJobSearchRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RankedJobSearchRunRepository extends JpaRepository<RankedJobSearchRun, Long> {

    List<RankedJobSearchRun> findBySearchProfile_IdOrderByCreatedAtDesc(Long searchProfileId);
}
