package com.careerflow.searchprofile.service;

import com.careerflow.searchprofile.dto.SearchProfileCreateRequest;
import com.careerflow.searchprofile.dto.SearchProfileResponse;
import com.careerflow.searchprofile.entity.SearchProfile;
import com.careerflow.searchprofile.repository.SearchProfileRepository;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchProfileService {

    private final SearchProfileRepository repository;

    public SearchProfileService(SearchProfileRepository repository) {
        this.repository = repository;
    }

    /**
     * Create and save a search profile from the request.
     * Validates that required fields are present.
     *
     * @param request the search profile creation request
     * @return SearchProfileResponse with saved profile data
     */
    public SearchProfileResponse createSearchProfile(SearchProfileCreateRequest request) {
        // Validation happens via @Valid in controller using Hibernate Validator
        // Additional logic here if needed

        SearchProfile profile = new SearchProfile(
                request.getSummary(),
                request.getSearchRoles(),
                request.getAlternativeCareerRoles(),
                request.getKeywords()
        );

        SearchProfile saved = repository.save(profile);

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public SearchProfile getSearchProfileEntityById(Long id) {
        SearchProfile profile = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Search profile not found with id: " + id
                ));

        profile.getSearchRoles().size();
        profile.getAlternativeCareerRoles().size();
        profile.getKeywords().size();

        return profile;
    }

    /**
     * Map SearchProfile entity to SearchProfileResponse DTO
     *
     * @param profile the entity
     * @return the response DTO
     */
    private SearchProfileResponse mapToResponse(SearchProfile profile) {
        return new SearchProfileResponse(
                profile.getId(),
                profile.getSummary(),
                profile.getSearchRoles(),
                profile.getAlternativeCareerRoles(),
                profile.getKeywords(),
                profile.getCreatedAt()
        );
    }

}

