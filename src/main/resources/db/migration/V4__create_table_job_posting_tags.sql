CREATE TABLE IF NOT EXISTS job_posting_tags (
                                                job_posting_id BIGINT NOT NULL,
                                                tag_id BIGINT NOT NULL,
                                                PRIMARY KEY (job_posting_id, tag_id),
    CONSTRAINT fk_jpt_job_posting
    FOREIGN KEY (job_posting_id)
    REFERENCES job_postings(id)
    ON DELETE CASCADE,
    CONSTRAINT fk_jpt_tag
    FOREIGN KEY (tag_id)
    REFERENCES tags(id)
    ON DELETE CASCADE
    );
