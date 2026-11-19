# Aura Hub

Aura Hub is a boutique and private-client management app for **Maison Aura**, a fictional luxury fashion house. It combines a public lookbook with a staff workspace for managing collections, pieces, boutiques, stock, clients, orders and team accounts.

## Features

- **Public lookbook** — browse collections and pieces without signing in
- **Staff workspace** — manage collections, pieces, boutiques, stock, clients and orders
- **Team accounts** — JWT-based authentication with an initial admin account
- **Image uploads** — piece and collection images stored on disk
- **Optional email** — notifications through any SMTP provider

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3, Spring Security (JWT), Spring Data JPA |
| Database | MySQL 8 locally; PostgreSQL on Render |
| Frontend | Angular 15, TypeScript |

## Project structure

```
.
├── backend/backend      Spring Boot API (Maven)
│   ├── sql/             Demo seed script
│   └── uploads/         Demo images used by the seed data
└── frontend/modern      Angular application
```

## Prerequisites

- JDK 17 or newer
- Maven 3.9 or newer
- Node.js 18.10 or newer and npm
- MySQL 8 running locally on port 3306

## Getting started (Windows)

### 1. Start the backend

Make sure MySQL is running, then in a PowerShell terminal:

```powershell
cd backend\backend
$env:SERVER_PORT = "8081"
# Only if your local MySQL account needs credentials:
# $env:DB_USER = "root"
# $env:DB_PASSWORD = "your-local-mysql-password"
mvn spring-boot:run
```

On first start the backend creates the `aurahub` database and its tables, as long as the MySQL user is allowed to create databases. The defaults are `localhost:3306`, database `aurahub`, user `root` and an empty password.

### 2. Start the frontend

In a second terminal:

```powershell
cd frontend\modern
npm ci
npm start
```

The development environment points to `http://localhost:8081/api`. If you run the backend on another port, update `apiUrl` in `frontend\modern\src\environments\environment.ts`.

### 3. Open the app

| | URL |
|---|---|
| App | <http://localhost:4200> |
| Public lookbook | <http://localhost:4200/lookbook> |
| API | <http://localhost:8081/api> |

## Demo data (optional)

Start the backend once first so Hibernate creates the tables. Then, from the project root, run the seed script **once, on a fresh database**:

```powershell
cmd /c 'mysql -u root -p aurahub < "backend\backend\sql\aurahub_seed.sql"'
```

Enter your MySQL password when prompted (press Enter if it is blank). If `mysql` is not on your `PATH`, use the full path to `mysql.exe`, and change `-u root` if you use another database user.

> The seed inserts records with fixed IDs, so running it again on a populated database will fail or create conflicts.

It loads sample boutiques, stock, clients, collections, pieces and orders for the Overview page.

**Demo sign-in accounts**

| Role | Email | Password |
|---|---|---|
| Admin | Set with `ADMIN_EMAIL` | Set with `ADMIN_PASSWORD` on first startup |

## Images and uploads

The seed data references the demo images in `backend\backend\uploads`, which are committed to Git for that reason. Any other uploaded files are excluded by `.gitignore`, so runtime uploads are never pushed by accident.

By default the backend writes uploads to an `uploads` folder relative to its working directory (`backend\backend\uploads` when started from there). Set `UPLOAD_DIR` to use a different, persistent location.

## Configuration

The backend is configured through environment variables:

| Variable | Purpose |
|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | Database connection; PostgreSQL in production |
| `DB_URL` | Optional full JDBC URL (e.g. `jdbc:postgresql://...`) |
| `PORT` | HTTP port supplied by Render (default `8080` locally) |
| `JWT_SECRET` | Key used to sign JWTs |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Initial admin account |
| `UPLOAD_DIR` | Directory for uploaded images |
| `CORS_ORIGINS` | Comma-separated allowed browser origins |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASSWORD`, `MAIL_FROM` | Optional email settings |

### Security notes

- The defaults in the repository are for **local development only**.
- For any shared or deployed environment, set a strong `JWT_SECRET`, a private `ADMIN_PASSWORD` and your own database credentials.
- Never commit `.env` files or real credentials.

## Build for production

**Backend**

```powershell
cd backend\backend
mvn clean package
```

**Frontend**

```powershell
cd frontend\modern
npm ci
npm run build
```

The Angular build is written to `frontend\modern\dist\aurahub`.

## Deploy to Render

The Docker image builds Angular and Spring Boot together, so the app and `/api` share one origin. Use a Render web service and a managed Render PostgreSQL database; the database must be in the same region as the web service.

### 1. Create the database

In the Render Dashboard, create a **PostgreSQL** database. Choose a production plan with backups, and keep the database private. Do not use an expiring/free database for important data. Copy its **internal** host, port, database name, username and password from the database's Connect page.

### 2. Create the web service

Create a Web Service from this Git repository and configure:

| Setting | Value |
|---|---|
| Runtime | Docker |
| Root Directory | Leave blank (repository root) |
| Dockerfile Path | `backend/backend/Dockerfile` |
| Docker Context Directory | `.` |
| Health Check Path | `/actuator/health` |

### 3. Add Render environment variables

Set these on the web service (use Render's secret fields for passwords):

| Key | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `production` |
| `DB_HOST` | Internal hostname from the PostgreSQL Connect page |
| `DB_PORT` | Internal port, usually `5432` |
| `DB_NAME` | Database name from the Connect page |
| `DB_USER` | Database username from the Connect page |
| `DB_PASSWORD` | Database password from the Connect page |
| `JWT_SECRET` | Output of `openssl rand -hex 32` (64 random characters); keep it secret and stable |
| `ADMIN_EMAIL` | Your administrator email |
| `ADMIN_PASSWORD` | A unique, strong password; do not use the example/local default |
| `BASE_URL` | The web service URL, e.g. `https://aurahub.onrender.com` |
| `CORS_ORIGINS` | The same web service URL, with no trailing slash |
| `UPLOAD_DIR` | `/var/data/uploads` |

Render supplies `PORT` automatically. Do not set it manually. `DB_URL` is optional; if used, it must be a JDBC URL such as `jdbc:postgresql://HOST:5432/DB?sslmode=require`, not the `postgres://` URL shown by Render.

### 4. Persist uploaded images

In the web service's **Disks** settings, add a persistent disk mounted at `/var/data`. Without this disk, uploaded images are lost on deploy/restart. Keep the web service at one instance when using a local persistent disk; for horizontal scaling, move uploads to object storage (for example S3-compatible storage) instead.

### 5. Deploy and verify

Deploy the service and wait for `/actuator/health` to report `UP`. Open the service URL and sign in using `ADMIN_EMAIL` and the initial `ADMIN_PASSWORD`. The first boot creates the schema, roles and initial admin; later boots do not reset that password. Change it after first login if the app supports it, and keep the database, disk and JWT secret backed up/secured.

Do not run the demo MySQL seed script against the Render PostgreSQL database. The application currently uses Hibernate `ddl-auto=update` to create/update tables at startup; before making ongoing production schema changes, add and adopt versioned Flyway/Liquibase migrations and test restoring database backups. Database backups do not include uploaded images, so back up the persistent disk (or object storage) separately.

Configure a real SMTP provider through `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASSWORD` and `MAIL_FROM` if staff-registration notifications are needed. The development Mailtrap defaults are not a production mail service.