# MediCycle Backend

Spring Boot REST API for MediCycle.

Set MySQL environment variables first:

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="YOUR_PASSWORD"
```

Then run:

```powershell
mvn spring-boot:run
```

API base URL:

```text
http://localhost:8080/api
```

Important packages:

- `entity` — JPA model
- `repository` — Spring Data JPA
- `service` — business logic
- `controller` — REST API
- `config` / `security` — Spring Security, CSRF and CORS
- `exception` — centralized error responses
