# Student Task Manager

A production-ready student task dashboard built with Java 17, Spring Boot 3, Maven, Spring Security session authentication, JPA, and a responsive vanilla HTML/CSS/JavaScript frontend served from Spring Boot static resources.

## Features

- Secure sign-in/sign-up gate before any dashboard page or dashboard asset is served.
- Task CRUD with title, description, category, priority, due date, and completion state.
- Real-time completion progress bar.
- "Upcoming This Week" task filter for deadlines due in the next 7 days.
- Light/dark global theme toggle.
- Sticky current-month calendar with due-date markers.
- Database-backed notification preference screen for digest frequency, category reminders, and timezone.
- No SMTP, mail ports, or external mail server configuration.

## Local Run

Use Java 17.

```bash
./mvnw clean package
java -jar target/student-task-manager-0.0.1-SNAPSHOT.jar
```

If Maven is already installed and you do not use the wrapper script:

```bash
mvn clean package
mvn spring-boot:run
```

Open `http://localhost:8080`.

## Environment Variables

The app reads database and deployment settings from environment variables:

| Variable | Purpose | Default |
| --- | --- | --- |
| `PORT` | Web server port, used automatically by Render | `8080` |
| `SPRING_DATASOURCE_URL` | Preferred JDBC database URL | local H2 file database |
| `SPRING_DATASOURCE_USERNAME` | Database username | `sa` for H2 |
| `SPRING_DATASOURCE_PASSWORD` | Database password | empty for H2 |
| `DATABASE_URL` | Render-style database URL, including `postgres://...` | optional |
| `DATABASE_USERNAME` | Fallback database username | optional |
| `DATABASE_PASSWORD` | Fallback database password | optional |
| `JPA_DDL_AUTO` | Hibernate schema strategy | `update` |

For Render PostgreSQL, either set `SPRING_DATASOURCE_URL` to a JDBC URL or provide Render's `DATABASE_URL`; the application converts `postgres://user:pass@host:port/db` to a JDBC PostgreSQL URL.

## Render Deployment

Render's current native runtimes do not include Java/JVM applications, so this repository includes a Dockerfile and `render.yaml` for Render deployment.

Recommended Render settings:

- Runtime: Docker
- Dockerfile path: `./Dockerfile`
- Health check path: `/`
- Database: Render PostgreSQL, exposed to the app as `DATABASE_URL`

The app uses `server.port=${PORT:8080}`, so it will bind to Render's assigned port automatically.

For a Java-capable host outside Render, use:

- Build command: `sh ./mvnw clean package -DskipTests`
- Start command: `java -jar target/student-task-manager-0.0.1-SNAPSHOT.jar`


Github Link: https://github.com/ryanhelou2007-byte/student-task-manager
Fully working web application link:   https://student-task-manager-1-aoya.onrender.com     


