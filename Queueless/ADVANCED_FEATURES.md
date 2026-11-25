# QueueLess AI — Java-only advanced features

Java 17 + Spring Boot + MySQL. Frontend is browser HTML/CSS/vanilla JavaScript (no Python). All prediction, simulation, forecasting and optimization algorithms are Java.

1. **Personalized waiting prediction**: weighted historical service duration + current queue/counter demand; uncertainty interval. This is a transparent statistical estimator, **not a trained Random Forest**.
2. **Digital twin**: deterministic discrete-event scenario simulation for 1–5 counters.
3. **Counter allocation**: suggests minimum simulated counter count with <=15-minute average wait and <=85% utilization, otherwise maximum.
4. **Best time to visit**: labels upcoming hours based on recorded arrival-hour counts; sparse data can mislead.
5. **QR arrival check-in**: PNG QR generation; staff enters decoded payload from scanner, then backend verifies it. Not cryptographically signed; demo only.
6. **Congestion forecasting**: uses recent three-hour arrivals to estimate hourly arrivals and risk.
7. **Explainable analytics**: exposes wait prediction factors, scenarios, baseline MAE endpoint. Does not falsely claim validated ML accuracy.

## Advanced API
GET /api/intelligence/{serviceId} (authenticated)
GET /api/visits/{serviceId} (authenticated)
GET /api/tokens/{id}/qr (token owner or admin)
POST /api/staff/checkin {"qrValue":"QUEUELESS:23"} (staff/admin)
GET /api/admin/accuracy (admin)

## Limitations / review honesty
Not production ready: demo basic auth, no CSRF protection, QR payload forgeable, simplistic forecasting and simulation assumptions, no persistent prediction snapshots or real-world accuracy validation. Do not claim trained ML. Use HTTPS, role restrictions, signed expiring QR codes, migrations and full integration tests before production.
