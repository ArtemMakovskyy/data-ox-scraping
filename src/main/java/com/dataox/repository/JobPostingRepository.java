package com.dataox.repository;

import com.dataox.model.JobPosting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    boolean existsByJobPageUrl(String jobPageUrl);

    @EntityGraph(attributePaths = {"locations", "tags"})
    List<JobPosting> findAll();

    @EntityGraph(attributePaths = {"locations", "tags"})
    Optional<JobPosting> findById(Long id);
}
