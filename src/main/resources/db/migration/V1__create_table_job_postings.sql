CREATE TABLE IF NOT EXISTS job_postings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    job_page_url TEXT,
    position_name VARCHAR(255),
    organization_url VARCHAR(1000),
    logo_url VARCHAR(1000),
    organization_title VARCHAR(255),
    labor_function VARCHAR(255),
    posted_date_unix BIGINT,
    description_html TEXT
    );