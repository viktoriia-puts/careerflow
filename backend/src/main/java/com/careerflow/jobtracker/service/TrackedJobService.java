package com.careerflow.jobtracker.service;

import com.careerflow.jobtracker.dto.TrackedJobCreateRequest;
import com.careerflow.jobtracker.dto.TrackedJobResponse;
import com.careerflow.jobtracker.dto.TrackedJobUpdateRequest;
import com.careerflow.jobtracker.entity.TrackedJob;
import com.careerflow.jobtracker.entity.TrackedJobStatus;
import com.careerflow.jobtracker.repository.TrackedJobRepository;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.careerflow.searchprofile.repository.SearchProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TrackedJobService {

    private final TrackedJobRepository trackedJobRepository;
    private final SearchProfileRepository searchProfileRepository;

    public TrackedJobService(
            TrackedJobRepository trackedJobRepository,
            SearchProfileRepository searchProfileRepository
    ) {
        this.trackedJobRepository = trackedJobRepository;
        this.searchProfileRepository = searchProfileRepository;
    }

    @Transactional
    public TrackedJobResponse createTrackedJob(TrackedJobCreateRequest request) {
        validateCreateRequest(request);

        Long profileId = request.getSearchProfileId();
        TrackedJob existing = findExistingTrackedJob(request);
        if (existing != null) {
            return mapToResponse(existing);
        }

        TrackedJob trackedJob = new TrackedJob();
        trackedJob.setSearchProfile(getProfile(profileId));
        trackedJob.setCompany(truncate(request.getCompany().trim(), 500));
        trackedJob.setPositionTitle(truncate(request.getPositionTitle().trim(), 500));
        trackedJob.setLocation(truncate(request.getLocation(), 500));
        trackedJob.setSource(truncate(request.getSource(), 100));
        trackedJob.setJobUrl(request.getJobUrl());
        trackedJob.setReferenceId(truncate(request.getReferenceId(), 500));
        trackedJob.setMatchScore(request.getMatchScore());
        trackedJob.setStatus(
                request.getStatus() == null ? TrackedJobStatus.SAVED : request.getStatus()
        );
        trackedJob.setNotes(request.getNotes());

        return mapToResponse(trackedJobRepository.save(trackedJob));
    }

    @Transactional(readOnly = true)
    public List<TrackedJobResponse> getTrackedJobs(Long profileId) {
        List<TrackedJob> jobs = profileId == null
                ? trackedJobRepository.findAllByOrderByCreatedAtDesc()
                : trackedJobRepository.findBySearchProfileIdOrderByCreatedAtDesc(profileId);

        return jobs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public TrackedJobResponse updateTrackedJob(
            Long id,
            TrackedJobUpdateRequest request
    ) {
        TrackedJob trackedJob = trackedJobRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tracked job with id " + id + " not found"
                ));

        if (request.getCompany() != null && !request.getCompany().isBlank()) {
            trackedJob.setCompany(truncate(request.getCompany().trim(), 500));
        }

        if (request.getPositionTitle() != null && !request.getPositionTitle().isBlank()) {
            trackedJob.setPositionTitle(truncate(request.getPositionTitle().trim(), 500));
        }

        if (request.getLocation() != null) {
            trackedJob.setLocation(truncate(request.getLocation(), 500));
        }

        if (request.getJobUrl() != null) {
            trackedJob.setJobUrl(request.getJobUrl());
        }

        if (request.getMatchScore() != null) {
            trackedJob.setMatchScore(request.getMatchScore());
        }

        if (request.getStatus() != null) {
            trackedJob.setStatus(request.getStatus());
        }

        trackedJob.setAppliedDate(request.getAppliedDate());
        trackedJob.setResultNote(truncate(request.getResultNote(), 500));
        trackedJob.setNotes(request.getNotes());

        return mapToResponse(trackedJob);
    }

    @Transactional
    public void deleteTrackedJob(Long id) {
        if (!trackedJobRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tracked job with id " + id + " not found"
            );
        }

        trackedJobRepository.deleteById(id);
    }

    private TrackedJob findExistingTrackedJob(TrackedJobCreateRequest request) {
        Long profileId = request.getSearchProfileId();

        if (hasText(request.getReferenceId()) && hasText(request.getSource())) {
            return trackedJobRepository
                    .findFirstBySearchProfileIdAndSourceAndReferenceId(
                            profileId,
                            request.getSource().trim(),
                            request.getReferenceId().trim()
                    )
                    .orElse(null);
        }

        if (hasText(request.getJobUrl())) {
            return trackedJobRepository
                    .findFirstBySearchProfileIdAndJobUrl(
                            profileId,
                            request.getJobUrl().trim()
                    )
                    .orElse(null);
        }

        return null;
    }

    private SearchProfile getProfile(Long profileId) {
        if (profileId == null) {
            return null;
        }

        return searchProfileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Search profile with id " + profileId + " not found"
                ));
    }

    private void validateCreateRequest(TrackedJobCreateRequest request) {
        if (request == null
                || !hasText(request.getCompany())
                || !hasText(request.getPositionTitle())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Company and position title are required"
            );
        }
    }

    private TrackedJobResponse mapToResponse(TrackedJob job) {
        Long profileId = job.getSearchProfile() == null
                ? null
                : job.getSearchProfile().getId();

        return new TrackedJobResponse(
                job.getId(),
                profileId,
                job.getCompany(),
                job.getPositionTitle(),
                job.getLocation(),
                job.getSource(),
                job.getJobUrl(),
                job.getReferenceId(),
                job.getMatchScore(),
                job.getStatus(),
                job.getAppliedDate(),
                job.getResultNote(),
                job.getNotes(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }

        return trimmed.substring(0, maxLength);
    }
}
