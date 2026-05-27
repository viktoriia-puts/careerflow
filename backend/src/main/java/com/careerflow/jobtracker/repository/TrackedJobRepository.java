package com.careerflow.jobtracker.repository;

import com.careerflow.jobtracker.entity.TrackedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackedJobRepository extends JpaRepository<TrackedJob, Long> {

    List<TrackedJob> findAllByOrderByCreatedAtDesc();

    List<TrackedJob> findBySearchProfileIdOrderByCreatedAtDesc(Long searchProfileId);

    Optional<TrackedJob> findFirstBySearchProfileIdAndSourceAndReferenceId(
            Long searchProfileId,
            String source,
            String referenceId
    );

    Optional<TrackedJob> findFirstBySearchProfileIdAndJobUrl(
            Long searchProfileId,
            String jobUrl
    );
}
