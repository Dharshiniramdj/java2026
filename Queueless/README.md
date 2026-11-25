# QueueLess — Java-Based Intelligent Queue Management & Wait-Time Prediction System

**Capstone Project Review 1 — SIMATS Engineering (Saveetha Institute of Medical and Technical Sciences)**  
**Authors:** Dharshini J (192320459) & Rajeswari P (192311072)  
**Supervisor / Guide:** Vijayaragavan N  

---

## 1. Project Overview

**QueueLess** is an intelligent, full-stack queue management and decision-support platform designed for high-density public service environments such as banks, outpatient clinics, and government administration centers.

Unlike traditional ticket dispensers that only display static numbers and fail to handle surging crowds, QueueLess provides **7 Advanced Capabilities**:
1. **Dynamic Wait-Time Prediction:** Multi-factor calculation accounting for queue depth, active counters, consultation duration, and congestion drift.
2. **Interactive Discrete-Event Digital Twin Simulator:** Models arrival, queue entry, and service completion events in pure Java using a priority queue.
3. **Dynamic Counter Allocation & Optimization:** Automatically evaluates staffing configurations to maintain average wait $\le 12\text{ min}$ and utilization $\le 85\%$.
4. **Best-Time-to-Visit Recommendations:** Aggregates historical arrival distributions across operating hours to highlight low-crowd visiting slots.
5. **Cryptographic QR Arrival Verification:** Prevents phantom queue delays by validating physical arrival at the counter using ZXing QR tokens.
6. **Predictive Congestion Forecasting:** Projects queue pressure across +15, +30, +45, and +60-minute horizons.
7. **Explainable Queue Analytics:** Transparent factor decomposition showing exactly why a wait time increased or decreased.

---

## 2. Technology Stack

- **Backend:** Java 17, Spring Boot 3.3.5
- **Persistence:** Embedded Persistent H2 Database (`./data/queueless.mv.db`) with optional MySQL support
- **ORM / Data Access:** Spring Data JPA, Hibernate ORM
- **Security:** Spring Security with BCrypt password hashing & Role-Based Access Control (`CUSTOMER`, `STAFF`, `ADMIN`)
- **Real-Time Communication:** STOMP over WebSockets for zero-refresh dashboard synchronization
- **Data Visualization:** Chart.js 4.4
- **QR Code Engine:** Google ZXing (Zebra Crossing) 3.5.3
- **Reporting Engine:** OpenPDF / LibrePDF
- **Testing:** JUnit 5 (12/12 passing algorithm & unit tests)
- **Presentation:** Automated 16:9 Widescreen PowerPoint (`QueueLess_Capstone_Presentation.pptx`)

---

## 3. Demo Credentials (Local Development)

| Role | Email Address | Password | Permissions & Dashboard |
| :--- | :--- | :--- | :--- |
| **Customer** | `customer@demo.com` | `Customer@123` | Module 1: Token generation, live progress stepper, QR modal, feedback |
| **Staff** | `staff@demo.com` | `Staff@123` | Module 2: Call next, consultation stopwatch, skip/recall, QR scanner |
| **Administrator** | `admin@demo.com` | `Admin@123` | Module 3: Digital Twin simulator, KPI cards, Chart.js trends, CSV/PDF export |

---

## 4. One-Click Quick Start (Windows 11 Home)

### Prerequisites
- **Java 17 or higher** (e.g. Eclipse Adoptium Temurin 17 or Oracle JDK 17).
- No Docker, no WSL, and no MySQL server installation required!

### Execution Steps
1. **One-Click Launch:** Double-click `RUN_QUEUELESS.bat`.
   - The launcher automatically detects Java 17 on your machine (even if not configured in PATH).
   - Starts the embedded Spring Boot server on `http://localhost:8080`.
   - Automatically opens your default web browser to the application!
2. **Automated Setup / Rebuild (Optional):** Run `SETUP_WINDOWS.bat` to run all JUnit tests and rebuild the JAR.
3. **Clean Shutdown:** Double-click `STOP_QUEUELESS.bat`.

---

## 5. Review 1 Slide Deck

The editable PowerPoint presentation is generated in:
`QueueLess_Capstone_Presentation.pptx`

It features:
- Institutional header: SIMATS Engineering • Capstone Project Review 1
- Full team attribution: Dharshini J (192320459) & Rajeswari P (192311072)
- Supervisor attribution: Vijayaragavan N
- 5 Verified research papers (Balthasar 2026, Rao 2026, Taton 2025, Kerner 2025, Vázquez-Serrano 2024)
- Comprehensive speaker notes on every single slide
- System architecture, mathematical formulas, and performance comparison tables

---

## 6. Review 2 Forward Roadmap

- Automated SMS dispatch via Twilio / Fast2SMS gateway.
- Multi-branch multi-tenancy (`branch_id` scoping).
- Empirical clinic dataset validation.
- Progressive Web App (PWA) packaging for mobile devices.
