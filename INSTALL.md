# Instructions for Running the **data-ox-scraping** Project

## Requirements

- **Docker** – used only for running the MySQL database.
- **Java 17** – required to run the application.
- **Maven** – used to build and launch the application.

---

## Steps to Run

### 1. Clone the Repository

```bash
git clone https://github.com/ArtemMakovskyy/data-ox-scraping.git
cd data-ox-scraping
```

### 2. Start the MySQL Database with Docker

Make sure Docker is running, then execute:

```bash
docker-compose up
```

> This will start a MySQL container required for the application to work.

---

### 3. Make Sure You Have Installed:

- Java 17
- Maven

---

### 4. Run the Application

From the root of the project, run:

```bash
mvn spring-boot:run
```

---

### 5. Open the Form in Your Browser

Once the application is running, open:

[http://localhost:8082/form](http://localhost:8082/form)

---

### 6. Start Scraping

Select the desired categories in the form — data will be automatically saved to the MySQL database.