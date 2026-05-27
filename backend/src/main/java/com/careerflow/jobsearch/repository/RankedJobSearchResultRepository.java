package com.careerflow.jobsearch.repository;

import com.careerflow.jobsearch.entity.RankedJobSearchResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankedJobSearchResultRepository extends JpaRepository<RankedJobSearchResultEntity, Long> {

    List<RankedJobSearchResultEntity> findBySearchRun_IdOrderByPositionIndexAsc(Long searchRunId);

    Optional<RankedJobSearchResultEntity> findByIdAndSearchRun_SearchProfile_Id(Long id, Long searchProfileId);

    List<RankedJobSearchResultEntity> findBySearchRun_SearchProfile_Id(Long searchProfileId);
}
