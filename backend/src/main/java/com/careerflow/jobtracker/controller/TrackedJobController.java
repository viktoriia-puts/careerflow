package com.careerflow.jobtracker.controller;

import com.careerflow.jobtracker.dto.TrackedJobCreateRequest;
import com.careerflow.jobtracker.dto.TrackedJobResponse;
import com.careerflow.jobtracker.dto.TrackedJobUpdateRequest;
import com.careerflow.jobtracker.service.TrackedJobService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tracked-jobs")
public class TrackedJobController {

    private final TrackedJobService trackedJobService;

    public TrackedJobController(TrackedJobService trackedJobService) {
        this.trackedJobService = trackedJobService;
    }

    @PostMapping
    public ResponseEntity<TrackedJobResponse> createTrackedJob(
            @RequestBody TrackedJobCreateRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(trackedJobService.createTrackedJob(request));
    }

    @GetMapping
    public ResponseEntity<List<TrackedJobResponse>> getTrackedJobs(
            @RequestParam(required = false) Long profileId
    ) {
        return ResponseEntity.ok(trackedJobService.getTrackedJobs(profileId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrackedJobResponse> updateTrackedJob(
            @PathVariable Long id,
            @RequestBody TrackedJobUpdateRequest request
    ) {
        return ResponseEntity.ok(trackedJobService.updateTrackedJob(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrackedJob(@PathVariable Long id) {
        trackedJobService.deleteTrackedJob(id);
        return ResponseEntity.noContent().build();
    }
}
