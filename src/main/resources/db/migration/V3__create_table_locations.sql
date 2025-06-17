CREATE TABLE IF NOT EXISTS locations (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         address VARCHAR(500),
    job_posting_id BIGINT,
    CONSTRAINT fk_location_job_posting
    FOREIGN KEY (job_posting_id)
    REFERENCES job_postings(id)
    ON DELETE CASCADE
    );
