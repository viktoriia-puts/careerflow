package com.careerflow.querygeneration.repository;

import com.careerflow.querygeneration.entity.SearchProfileGeneratedQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchProfileGeneratedQueryRepository extends JpaRepository<SearchProfileGeneratedQuery, Long> {

    List<SearchProfileGeneratedQuery> findBySearchProfileIdOrderByQueryTypeAscPositionIndexAsc(Long searchProfileId);

    void deleteBySearchProfileId(Long searchProfileId);
}
