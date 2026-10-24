# 🏏 Cricbuzz Cricket Dashboard

A full-stack cricket dashboard built using **Java, Spring Boot, MySQL, HTML, CSS, and JavaScript**. This project provides REST APIs for managing cricket matches, teams, and players, along with an interactive web interface for viewing and managing match information.

## 🚀 Features

- 🏏 **Match Management** — Create, view, update, and delete cricket matches.
- 📊 **Score Management** — Update runs, wickets, and overs.
- 🔴 **Match Status Management** — Manage upcoming, live, and completed matches.
- 🏆 **Team Management** — Manage cricket teams.
- 👤 **Player Management** — Manage player information.
- 🔍 **Search and Filtering** — Find matches using the dashboard's available filters.
- 📄 **Pagination and Sorting** — Navigate match and player data where supported.
- 📱 **Interactive Dashboard** — View match information through a web interface.
- 🔌 **REST API** — Backend APIs built using Spring Boot.
- 🗄️ **MySQL Database** — Store match, team, and player information.

## 🛠️ Tech Stack

### Backend
- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- Maven
- REST APIs

### Frontend
- HTML5
- CSS3
- JavaScript

### Database and Tools
- MySQL
- MySQL Workbench
- IntelliJ IDEA
- Postman
- Git and GitHub
- Docker

## 📂 Project Structure

```text
Cricbuzz-Cricket-Dashboard/
│
├── Crickbuzz-Backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/swapnil/cricbuzz/
│   │   │   │   ├── config/
│   │   │   │   ├── controller/
│   │   │   │   ├── dto/
│   │   │   │   ├── entity/
│   │   │   │   ├── exception/
│   │   │   │   ├── repository/
│   │   │   │   └── service/
│   │   │   └── resources/
│   │   └── test/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
│
└── Crickbuzz-Frontend/
    ├── index.html
    ├── style.css
    └── script.js
```

## ⚙️ Prerequisites

Before running this project, make sure you have installed:

- Java Development Kit (JDK 21, if required by your project configuration)
- MySQL Server
- IntelliJ IDEA or another Java IDE
- A modern web browser
- Postman (optional, for API testing)
- Docker (optional)

## 🗄️ Database Configuration

This project uses MySQL to store cricket match, team, and player information.

### Step 1: Create the database

Open MySQL Workbench and execute:

```sql
CREATE DATABASE cricbuzz;
```

If the database already exists, you do not need to create it again.

### Step 2: Configure the database connection

Open:

`Crickbuzz-Backend/src/main/resources/application.yml`

Configure your MySQL connection details.

Example:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/cricbuzz
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

This is an example configuration. Keep the existing settings your application requires, and use environment variables for database credentials.

**Security:** Never upload your actual database password, API keys, or other secrets to a public GitHub repository.

## ▶️ How to Run the Backend

1. Clone or download this repository.
2. Open the `Crickbuzz-Backend` folder containing `pom.xml` in IntelliJ IDEA.
3. Configure your MySQL database connection.
4. Ensure the database server is running.
5. Run the `CricbuzzApplication.java` main class.

The application may run at:

```text
http://localhost:8080
```

The actual port depends on your application configuration.

## 🌐 How to Run the Frontend

1. Open the `Crickbuzz-Frontend` folder.
2. Open `index.html` in your browser, or use the Live Server extension in VS Code.
3. Make sure the Spring Boot backend is running.
4. Check the API URLs in `script.js` to ensure they point to the correct backend address.

For local development, the API base URL may be:

```text
http://localhost:8080
```

The frontend and backend must be configured to communicate correctly. If they run on different ports, ensure the backend's CORS configuration allows the frontend origin.

## 🔌 REST API Endpoints

The backend provides APIs for managing matches, teams, and players.

### Match APIs

| HTTP Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/matches` | Retrieve matches |
| GET | `/api/v1/matches/{id}` | Retrieve a match by ID |
| POST | `/api/v1/matches` | Create a match |
| PUT | `/api/v1/matches/{id}` | Update a match |
| PATCH | `/api/v1/matches/{id}/score` | Update a match score |
| PATCH | `/api/v1/matches/{id}/status` | Update match status |
| DELETE | `/api/v1/matches/{id}` | Delete a match |

> Check `MatchController.java` to confirm that every method and endpoint matches the current implementation.

### Team APIs

Team management endpoints are implemented in `TeamController.java`.

### Player APIs

Player management endpoints are implemented in `PlayerController.java`.

For the exact request methods, paths, and request formats, refer to the respective controller classes or the project's API documentation.

## 🖼️ Screenshots

Screenshots of the running application can be added here to demonstrate the dashboard and its features.

Suggested screenshots:

- Main dashboard showing cricket matches
- Live match score
- Match creation or editing form
- Match details view

Create a `screenshots` folder in the repository and add your screenshots before linking them in this section.

Example Markdown syntax:

```markdown
![Cricbuzz Dashboard](screenshots/dashboard.png)
```

## 🧪 Testing

The project contains backend integration tests.

To run the Maven test suite, open a terminal in the backend project directory and execute:

```bash
mvn test
```

Ensure that any required test configuration and dependencies are available before running the tests.

## 🔮 Future Improvements

- Deploy the frontend and backend online.
- Improve mobile responsiveness.
- Add authentication and role-based authorization.
- Integrate a reliable live cricket data source.
- Add more automated tests.
- Improve monitoring and error handling.

## 👨‍💻 Author

**Swapnil Phad**

- GitHub: [Swapnil-77](https://github.com/Swapnil-77)
- LinkedIn: [Swapnil Phad](https://www.linkedin.com/in/swapnil-phad7/)

## ⭐ Support

If you find this project interesting, consider giving the repository a star!

---

**Built with Java, Spring Boot, MySQL, HTML, CSS, and JavaScript.**