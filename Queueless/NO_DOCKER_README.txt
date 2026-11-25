QUEUELESS - NO DOCKER VERSION

1. Install Java 17 and Maven. Close and reopen PowerShell.
2. Check: java -version ; mvn -version
3. Double click RUN_WITHOUT_DOCKER.bat or run mvn spring-boot:run in this folder.
4. Open http://localhost:8080
5. Database: embedded H2 file at ./data/queueless.mv.db. Data persists after restart.
6. Demo logins: admin@demo.com / Admin@123 ; staff@demo.com / Staff@123 ; customer@demo.com / Customer@123
7. To switch to MySQL later, start with --spring.profiles.active=mysql and supply DB_URL, DB_USER, DB_PASSWORD.

Note: requires Maven to download dependencies on first run. This build has not been executed in this environment.
