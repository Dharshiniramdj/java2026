# Demonstration and test checklist

1. Start MySQL and Spring Boot; log in as customer.
2. Generate a Banking token and confirm its queue position and predicted wait.
3. Display QR image and verify using the staff check-in endpoint.
4. Log in as staff, call the next token and complete it.
5. Confirm customer status changes and administrator metrics update.
6. Log in as administrator and inspect `/api/intelligence/1`.
7. Compare a custom scenario using `/api/admin/simulate/1` with JSON `{"counters":3,"arrivalsPerHour":25,"serviceMinutes":6}`.
8. Toggle a counter and check the changed active-counter count.
9. Export CSV/PDF and verify content.
10. Run `mvn clean test` and capture output for your report.

**Do not claim these checks passed until actually executed.**
