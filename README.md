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

    -----

## 📊 Monitoring and Observability

The platform includes a comprehensive monitoring stack using **Prometheus**, **Grafana**, and the **ELK Stack** (Elasticsearch, Logstash, Kibana), providing real-time metrics collection, visualization, and centralized log management.

-----

### 🔧 Configuration

All monitoring components are pre-configured in `docker-compose.yml` and rely on configuration files in the `./monitoring` directory.

| Component | Port | Purpose |
| :--- | :--- | :--- |
| **Prometheus** | `9090` | Metrics collection. Scrapes the Spring Boot backend's `/actuator/prometheus` endpoint. |
| **Grafana** | `3000` | Metrics visualization. Pulls time-series data from Prometheus for dashboards. |
| **Elasticsearch** | `9200` | Log storage. Centralized store for structured application logs. |
| **Logstash** | N/A | Log processing. Reads logs from `./logs/spring.log`, applies Grok filtering, and ships to Elasticsearch. |
| **Kibana** | `5601` | Log visualization. Web UI for searching and analyzing logs stored in Elasticsearch. |
| **SQLite Web UI** | `8086` | Database browser. Interactive interface for exploring the SQLite database. |

-----

### 🌐 Access the Monitoring Dashboards

| Dashboard | URL | Credentials |
| :--- | :--- | :--- |
| **Prometheus UI** | [http://localhost:9090](http://localhost:9090) | N/A |
| **Grafana UI** | [http://localhost:3000](http://localhost:3000) | `admin` / `admin` |
| **Kibana UI** | [http://localhost:5601](http://localhost:5601) | N/A |
| **SQLite Web UI** | [http://localhost:8086](http://localhost:8086) | N/A |

-----

### ⚙️ Backend Instrumentation

The Spring Boot backend is automatically instrumented for metrics and logging:

#### 1. **Metrics Collection**
The backend exposes health and performance metrics via **Spring Boot Actuator**, including:
- JVM memory usage and garbage collection stats
- HTTP request latency and throughput
- Database connection pool usage
- Custom application metrics

Prometheus scrapes the `/actuator/prometheus` endpoint every few seconds to collect these metrics.

#### 2. **Centralized Logging**
All application logs are written to the host volume `./logs/spring.log`. **Logstash** continuously reads this file and:
- Parses log entries using **Grok filters** to extract structured fields (timestamp, log level, message)
- Enriches log data with metadata
- Ships parsed logs to **Elasticsearch** for indexing and storage

-----

### ✅ Verification Steps

To confirm the full monitoring and logging pipeline is operational:

#### **1. Metrics Check (Prometheus)**
1. Navigate to [http://localhost:9090](http://localhost:9090)
2. Go to **Status → Targets**
3. Verify that the target `ft_backend` shows status **UP**
4. Test a query: Enter `jvm_memory_used_bytes` in the expression browser to view JVM memory metrics

#### **2. Dashboards Check (Grafana)**
1. Navigate to [http://localhost:3000](http://localhost:3000)
2. Login with credentials: `admin` / `admin`
3. Explore pre-configured dashboards for application metrics and performance monitoring

#### **3. Log Check (Kibana)**
1. Navigate to [http://localhost:5601](http://localhost:5601)
2. Go to **Discover**
3. Select the `spring-logs-*` data view
4. Verify that structured application logs are flowing in real-time with parsed fields (`timestamp`, `log_message`, etc.)
5. No `_grokparsefailure` or `_dateparsefailure` tags should appear in the logs

-----

### 🐛 Troubleshooting

| Issue | Solution |
| :--- | :--- |
| Prometheus shows target as **DOWN** | Check that the backend is running and `/actuator/prometheus` is accessible at `http://localhost:8080/actuator/prometheus` |
| Logs not appearing in Kibana | Verify Logstash is running: `docker logs ft_logstash`. Check for Grok parsing errors in Logstash output. |
| `_grokparsefailure` tags in logs | Review the Grok pattern in `./monitoring/logstash/logstash.conf` to ensure it matches your log format. |
| Grafana shows "No Data" | Confirm Prometheus is scraping metrics successfully and that the data source is configured correctly in Grafana. |

-----
