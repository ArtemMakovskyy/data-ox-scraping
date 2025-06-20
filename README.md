# Job Scraper for jobs.techstars.com

## Description

A Java (Spring Boot) application for scraping job listings from [jobs.techstars.com](https://jobs.techstars.com/jobs)
based on selected job functions.

Users can select a job function from a web form, after which the application automatically scrapes relevant job postings
for that category and saves them to an SQL database.

## Features

- Web interface to start scraping by job function at:  
  `http://localhost:8082/form`
- Allows sequential scraping by different job categories via the form.
- REST API to retrieve all jobs or a job by ID.
- Scraping results saved to an SQL database with schema; database dump available.
- Built with Spring Boot, Maven, and ORM for database interaction.
- MySql is available in Docker support.

## Web Interface for Scraping

- Job function selection form available at:  
  `http://localhost:8082/form`
- After selecting and submitting a job function, scraping starts for that category.
- It is recommended to clean the database via API before starting new scraping.

---

### GET All Jobs

```
GET http://localhost:8082/api/jobs
Content-Type: application/json
```

---

### GET Job by ID

```
GET http://localhost:8082/api/jobs/{id}
Content-Type: application/json
```

---

## Available Job Functions for Scraping

- Accounting & Finance
- Administration
- Compliance / Regulatory
- Customer Service
- Data Science
- Design
- IT
- Legal
- Marketing & Communications
- Operations
- Other Engineering
- People & HR

---
