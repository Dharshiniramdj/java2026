# 5-minute review demonstration

**00:00–00:45** Introduce QueueLess, its three roles and the problem of uncertain physical queues.

**00:45–01:30** Sign in as customer. Choose Banking and generate a token. Explain FIFO and queue position.

**01:30–02:30** Sign in as staff in another browser. Choose Bank Counter 1, call the next token, complete it, and demonstrate that customer status changes.

**02:30–03:15** Explain the rolling average: service times 4, 5, 6 minutes give mean 5; three customers ahead at one counter implies approximately 15 minutes.

**03:15–04:15** Show near-turn alert and WebSocket live refresh. Explain that email requires configured SMTP.

**04:15–05:00** Sign in as admin. Show prefilled analytics, peak hours, counter performance, and export CSV/PDF. End with future scope: SMS, mobile app, more accurate prediction.

## Panel questions
- **Why Spring Boot?** REST endpoints, security, JPA and WebSocket integration.
- **Why MySQL?** Relational integrity between users, services, counters, tokens and logs.
- **How is wait predicted?** Rolling average of last 20 completed service durations, adjusted for queue length and counter count.
- **What happens on skip?** Token becomes SKIPPED and no service duration is recorded.
- **What if server restarts?** MySQL data persists, but browsers reconnect manually after reload.
- **What is not complete?** Production security hardening, advanced forecasting, mobile app and SMS integration.
