# QueueLess AI — Advanced Java Capstone

## Requirements
Java 17, Maven 3.9+, Docker Desktop (MySQL 8 container).

## Windows PowerShell
```powershell
cd QueueLess_Project
docker compose up -d
mvn test
mvn spring-boot:run
```
Open http://localhost:8080. Seeded accounts: customer@demo.com / Customer@123; staff@demo.com / Staff@123; admin@demo.com / Admin@123.

## Demonstration
Customer: join queue, see wait estimate, QR, visit-hour recommendations. Staff: call/skip/complete tokens; paste decoded QR text (e.g. QUEUELESS:23) into arrival check-in. Admin: select service, run digital twin, compare counter scenarios, see risk, export reports.

## Notes
This is a capstone **prototype**, not a production deployment. All prediction and simulation algorithms are Java. The browser UI uses HTML/CSS/JavaScript. The predictor is statistical, not a trained ML model. Historical test records are synthetic. SMTP requires configuration. See ADVANCED_FEATURES.md for limitations and endpoint documentation.
