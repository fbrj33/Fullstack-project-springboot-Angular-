# Aura Hub — Angular workspace

The Angular application for Maison Aura's private client and boutique operations. It uses the Spring Boot API in `backend\backend`; the browser never connects directly to MySQL.

## Local setup

Prerequisites:

- Node.js 18.10 or later (Angular CLI 15)
- npm
- Java 17 or later and Maven
- MySQL 8, running on `localhost:3306`

### 1. Start the API and database

The backend creates the `aurahub` database and its tables on first start. Configure `DB_HOST`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` if your MySQL credentials differ from the local defaults.

From `backend\backend`:

```powershell
mvn spring-boot:run
```

The checked-in development environment points to `http://localhost:8081/api`, so start Spring with `$env:SERVER_PORT=8081`. The first start also creates the admin account configured in the backend's `application.properties`. If you use another port, update `apiUrl` in `src\environments\environment.ts` to match.

To load the optional sample boutiques, collections, pieces, stock, clients, and orders, use the backend's `sql\aurahub_seed.sql` after the first successful startup:

```powershell
cmd /c 'mysql -u root -p aurahub < "sql\aurahub_seed.sql"'
```

The sample advisor accounts are `marie@aura.com` and `youssef@aura.com`, both with password `Conseil123!`.

### 2. Start Angular

From `frontend\modern`:

```powershell
npm ci
npm start
```

Open `http://localhost:4200`. Development uses `http://localhost:8081/api`; the backend allows the Angular development origin by default. For another origin, set the backend's `CORS_ORIGINS`.

## Sign-in

The default administrator is `admin@aura.com` / `Admin123!`. Change these backend environment defaults before exposing the service. New account requests are disabled until an administrator activates them.

## Workspace features

- Public, price-free lookbook at `http://localhost:4200/lookbook`
- Overview with order, stock, revenue, and best-seller summaries
- Collection and piece catalog management
- Piece image uploads, plus boutique and size-level stock management
- Private client portfolios, scoped by advisor on the API
- Client profiles with purchase history and preferences
- Draft order creation with live size-level availability, plus confirm, cancel, and delivery actions
- Administrator-only user management and account activation

The API enforces authorization and business rules; hiding administrator actions in Angular is only a usability measure.

## Production build

```powershell
npm run build
```

The Angular build is written to `dist\aurahub`. Production uses the relative `/api` base URL, so serve it behind the backend or a reverse proxy that routes `/api` to Spring Boot.
