-----

# 🏓 ft\_transcendence – Multiplayer Pong Platform (Java Edition)

A Dockerized single-page web platform for playing real-time Pong, built with **React**, **Spring Boot 3 (Java 21)**, and **SQLite**. This repository represents the migration of the original project's backend from Node.js/Fastify to a robust, enterprise-grade Java architecture.

-----

## 🚀 Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Frontend** | **React / TypeScript** | Single-Page Application (SPA) built with Vite and Tailwind CSS. |
| **Backend** | **Spring Boot 3 (Java 21)** | RESTful API using Spring Data JPA, Services, and Controllers. |
| **Database** | **SQLite** | Simple, file-based database used for persistence (Entities, Stats, Matches). |
| **ORM** | **Hibernate / JPA** | Manages database entities and transactions. |
| **Authentication** | **JWT / Spring Security** | Stateless, token-based authentication with BCrypt hashing. |
| **DevOps** | **Docker Compose** | Orchestration for the Java backend and React frontend. |

-----

## ✅ Feature Progress

| Feature | Status | Implementation |
| :--- | :--- | :--- |
| SPA Architecture | ✅ Done | React frontend is ready to consume the Spring API. |
| **JWT Authentication** | ✅ Done | Full token creation, validation, and secure route protection implemented. |
| Secure Registration/Login | ✅ Done | Passwords hashed using BCryptPasswordEncoder. |
| **Database Structure** | ✅ Done | Entities, Repositories, and Services implemented for User, UserStats, PongMatch, and ArkanoidScore. |
| Core Game Logic | ✅ Done | Logic for saving Pong and Arkanoid scores, XP calculation, and stats updating is active. |
| Leaderboards & History | ✅ Done | API endpoints for viewing global leaderboards and personal match history. |
| CORS Configuration | ✅ Done | Frontend (`http://localhost:5173`) can communicate with the backend. |

-----

## ⚙️ Development Setup

This project uses Docker Compose to manage the frontend and the Java backend environments.

### Prerequisites

  * **Docker** and **Docker Compose**
  * **Java 21** (if running the backend locally outside Docker)
  * **Maven**

### 1\. Build and Run the Platform

Use the provided `make` command to build the Java application and start the entire stack:

```bash
# Builds the Spring Boot application and starts all services in detached mode
make up
```

### 2\. Access the Services

| Service | URL / Port | Description |
| :--- | :--- | :--- |
| **Frontend** | [http://localhost:5173](https://www.google.com/search?q=http://localhost:5173) | The main application interface. |
| **Backend API** | [http://localhost:8080](https://www.google.com/search?q=http://localhost:8080) | Spring Boot REST API entry point. |

-----

## 🛠️ Backend Development (Java)

If you need to run or test the Spring Boot application directly outside of Docker:

### 1\. Compile and Run with Maven

```bash
# Compile and package the application
mvn clean install

# Run the application (assuming you have Java 21 installed)
java -jar target/beakend-0.0.1-SNAPSHOT.jar
```

### 2\. Key Backend Components

| Component | Description |
| :--- | :--- |
| **`SecurityConfig.java`** | Defines JWT filters, CORS policy, and endpoint access rules (`/api/auth/**` is public; all others require authentication). |
| **`JwtTokenProvider.java`** | Utility class responsible for creating, parsing, and validating JWTs using the shared secret key. |
| **`UserRepository`** | JPA Repository used by `CustomUserDetailsService` to find user credentials during authentication. |
| **`UserController`** | Uses `SecurityContextHolder` to securely retrieve the authenticated user's ID (Long) to fetch personal data. |

-----

## 📖 Database & JPA

The project uses Hibernate as the JPA provider. Database access is managed entirely through Spring Data Repositories.

  * **Database:** `application.properties` points to a local SQLite file.
  * **Schema:** The database schema is managed automatically by Hibernate (`spring.jpa.hibernate.ddl-auto=update` or `validate`).
