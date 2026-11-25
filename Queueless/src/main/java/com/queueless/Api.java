package com.queueless;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.Duration;
import java.nio.charset.StandardCharsets;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;

@RestController
@RequestMapping("/api")
public class Api {

    private final Repositories.Users users;
    private final Repositories.Services services;
    private final Repositories.Counters counters;
    private final Repositories.Tokens tokens;
    private final Repositories.Logs logs;
    private final Repositories.Feedbacks feedbacks;
    private final PasswordEncoder encoder;
    private final SimpMessagingTemplate ws;
    private final JavaMailSender mail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public Api(
        Repositories.Users users,
        Repositories.Services services,
        Repositories.Counters counters,
        Repositories.Tokens tokens,
        Repositories.Logs logs,
        Repositories.Feedbacks feedbacks,
        PasswordEncoder encoder,
        SimpMessagingTemplate ws,
        JavaMailSender mail
    ) {
        this.users = users;
        this.services = services;
        this.counters = counters;
        this.tokens = tokens;
        this.logs = logs;
        this.feedbacks = feedbacks;
        this.encoder = encoder;
        this.ws = ws;
        this.mail = mail;
    }

    // -------------------------------------------------------------
    // Authentication & Current User
    // -------------------------------------------------------------

    public record Registration(String name, String email, String phone, String password) {}
    public record TokenRequest(Long serviceId, boolean isPriority, String priorityReason) {}
    public record StaffAction(Long counterId) {}
    public record TransferRequest(Long targetCounterId) {}
    public record FeedbackRequest(int rating, String comments) {}

    @PostMapping("/register")
    @Transactional
    public synchronized Map<String, Object> register(@RequestBody Registration r) {
        if (r.name() == null || r.email() == null || r.password() == null || r.password().length() < 6) {
            throw new IllegalArgumentException("Valid name, email and 6+ character password required.");
        }
        if (users.findByEmail(r.email().trim().toLowerCase()).isPresent()) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        var u = users.save(new Models.AppUser(
            r.email().trim().toLowerCase(),
            r.name().trim(),
            r.phone() != null ? r.phone().trim() : "",
            encoder.encode(r.password()),
            "CUSTOMER"
        ));
        return Map.of("id", u.id, "email", u.email, "name", u.name, "role", u.role);
    }

    private Models.AppUser currentUser(Authentication a) {
        if (a == null || a.getName() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthenticated");
        }
        return users.findByEmail(a.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication a) {
        var u = currentUser(a);
        return Map.of("id", u.id, "name", u.name, "email", u.email, "role", u.role);
    }

    @GetMapping("/services")
    public List<Models.ServiceType> getServices() {
        return services.findAll();
    }

    @GetMapping("/counters")
    public List<Map<String, Object>> getCounters() {
        return counters.findAll().stream().map(c -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.id);
            m.put("name", c.name);
            m.put("counterNumber", c.counterNumber);
            m.put("serviceId", c.service != null ? c.service.id : null);
            m.put("serviceName", c.service != null ? c.service.name : "");
            m.put("active", c.active);
            m.put("currentStaff", c.currentStaff != null ? c.currentStaff.name : null);
            return m;
        }).toList();
    }

    // -------------------------------------------------------------
    // MODULE 1: Customer Queue & Virtual Tokens
    // -------------------------------------------------------------

    @PostMapping("/tokens")
    @Transactional
    public synchronized Map<String, Object> createToken(Authentication a, @RequestBody TokenRequest r) {
        var user = currentUser(a);
        var s = services.findById(r.serviceId()).orElseThrow(() -> new IllegalArgumentException("Service not found"));

        // Check if customer already has a pending or active token for this service
        boolean active = tokens.findByCustomerIdOrderByIdDesc(user.id).stream()
            .anyMatch(t -> t.service.id.equals(s.id) && List.of("WAITING", "CALLED", "IN_SERVICE").contains(t.status));
        if (active) {
            throw new IllegalArgumentException("You already have an active or waiting token for " + s.name);
        }

        var t = new Models.Token(user, s, r.isPriority(), r.priorityReason());
        t.tokenNumber = generateTokenNumber(s);
        t = tokens.saveAndFlush(t);

        publishQueueUpdate(s.id);
        return tokenToMap(t);
    }

    private String generateTokenNumber(Models.ServiceType s) {
        char prefix = s.name.toUpperCase().charAt(0);
        long count = tokens.count() + 1;
        return String.format("%c%03d", prefix, count % 1000);
    }

    @PostMapping("/tokens/{id}/cancel")
    @Transactional
    public synchronized Map<String, Object> cancelToken(Authentication a, @PathVariable Long id) {
        var user = currentUser(a);
        var t = tokens.findById(id).orElseThrow(() -> new IllegalArgumentException("Token not found"));
        if (!user.role.equals("ADMIN") && !t.customer.id.equals(user.id)) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
        if (!"WAITING".equals(t.status)) {
            throw new IllegalArgumentException("Only waiting tokens can be cancelled.");
        }
        t.status = "CANCELLED";
        t.completedAt = LocalDateTime.now();
        tokens.saveAndFlush(t);
        publishQueueUpdate(t.service.id);
        return tokenToMap(t);
    }

    @GetMapping("/tokens/mine")
    public List<Map<String, Object>> myTokens(Authentication a) {
        var user = currentUser(a);
        return tokens.findByCustomerIdOrderByIdDesc(user.id).stream().map(this::tokenToMap).toList();
    }

    @PostMapping("/tokens/{id}/feedback")
    @Transactional
    public Map<String, Object> submitFeedback(Authentication a, @PathVariable Long id, @RequestBody FeedbackRequest r) {
        var user = currentUser(a);
        var t = tokens.findById(id).orElseThrow(() -> new IllegalArgumentException("Token not found"));
        if (!t.customer.id.equals(user.id)) {
            throw new org.springframework.security.access.AccessDeniedException("Not your token");
        }
        if (feedbacks.findByTokenId(id).isPresent()) {
            throw new IllegalArgumentException("Feedback already submitted for this token.");
        }
        var fb = feedbacks.save(new Models.Feedback(t, user, Math.max(1, Math.min(5, r.rating())), r.comments()));
        return Map.of("id", fb.id, "rating", fb.rating, "comments", fb.comments != null ? fb.comments : "");
    }

    // -------------------------------------------------------------
    // MODULE 2: Staff Counter Operations
    // -------------------------------------------------------------

    @GetMapping("/staff/queue/{serviceId}")
    public List<Map<String, Object>> staffQueue(@PathVariable Long serviceId) {
        return tokens.findActiveQueue(serviceId).stream().map(this::tokenToMap).toList();
    }

    @PostMapping("/staff/call/{serviceId}")
    @Transactional
    public synchronized Map<String, Object> callNext(@PathVariable Long serviceId, @RequestBody StaffAction action) {
        var staffUser = currentUser(null); // or injected
        var counter = counters.findById(action.counterId()).orElseThrow(() -> new IllegalArgumentException("Counter not found"));
        if (!counter.active) throw new IllegalArgumentException("Selected counter is currently inactive");

        // Prevent calling if counter already has a called/in-service customer
        boolean counterBusy = tokens.findByServiceIdOrderByIdAsc(serviceId).stream()
            .anyMatch(t -> List.of("CALLED", "IN_SERVICE").contains(t.status) && t.counter != null && t.counter.id.equals(counter.id));
        if (counterBusy) {
            throw new IllegalArgumentException("Please complete or skip the current customer at " + counter.name + " first.");
        }

        // Priority queue ordering: Priority customers first, then FIFO
        var queue = tokens.findActiveQueue(serviceId).stream()
            .filter(t -> "WAITING".equals(t.status))
            .toList();

        if (queue.isEmpty()) {
            throw new IllegalArgumentException("No waiting customers for this service.");
        }

        var nextToken = queue.get(0);
        nextToken.status = "CALLED";
        nextToken.counter = counter;
        nextToken.startedAt = LocalDateTime.now();
        tokens.saveAndFlush(nextToken);

        publishQueueUpdate(serviceId);
        notifyCustomerTurn(nextToken);
        return tokenToMap(nextToken);
    }

    @PostMapping("/staff/start/{id}")
    @Transactional
    public synchronized Map<String, Object> startService(@PathVariable Long id) {
        var t = tokens.findById(id).orElseThrow(() -> new IllegalArgumentException("Token not found"));
        if (!"CALLED".equals(t.status)) {
            throw new IllegalArgumentException("Customer must be CALLED before starting service");
        }
        t.status = "IN_SERVICE";
        tokens.saveAndFlush(t);
        publishQueueUpdate(t.service.id);
        return tokenToMap(t);
    }

    @PostMapping("/staff/{operation}/{id}")
    @Transactional
    public synchronized Map<String, Object> finishToken(@PathVariable String operation, @PathVariable Long id) {
        if (!List.of("complete", "skip").contains(operation)) {
            throw new IllegalArgumentException("Invalid operation: must be 'complete' or 'skip'");
        }
        var t = tokens.findById(id).orElseThrow(() -> new IllegalArgumentException("Token not found"));
        if (!List.of("CALLED", "IN_SERVICE").contains(t.status)) {
            throw new IllegalArgumentException("Token is not currently called or in service");
        }

        t.status = operation.equals("complete") ? "COMPLETED" : "SKIPPED";
        t.completedAt = LocalDateTime.now();

        if (operation.equals("complete")) {
            int durationSeconds = (int) Math.max(30, Duration.between(t.startedAt != null ? t.startedAt : t.createdAt, t.completedAt).getSeconds());
            int waitSeconds = (int) Math.max(0, Duration.between(t.createdAt, t.startedAt != null ? t.startedAt : t.completedAt).getSeconds());
            logs.save(new Models.ServiceLog(t, t.counter, null, durationSeconds, waitSeconds));
        }

        tokens.saveAndFlush(t);
        publishQueueUpdate(t.service.id);
        return tokenToMap(t);
    }

    @PostMapping("/staff/recall/{id}")
    @Transactional
    public synchronized Map<String, Object> recallToken(@PathVariable Long id) {
        var t = tokens.findById(id).orElseThrow(() -> new IllegalArgumentException("Token not found"));
        if (!"SKIPPED".equals(t.status)) {
            throw new IllegalArgumentException("Only SKIPPED tokens can be recalled");
        }
        t.status = "WAITING";
        t.startedAt = null;
        tokens.saveAndFlush(t);
        publishQueueUpdate(t.service.id);
        return tokenToMap(t);
    }

    @PostMapping("/staff/transfer/{id}")
    @Transactional
    public synchronized Map<String, Object> transferToken(@PathVariable Long id, @RequestBody TransferRequest r) {
        var t = tokens.findById(id).orElseThrow(() -> new IllegalArgumentException("Token not found"));
        var newCounter = counters.findById(r.targetCounterId()).orElseThrow(() -> new IllegalArgumentException("Target counter not found"));
        t.counter = newCounter;
        tokens.saveAndFlush(t);
        publishQueueUpdate(t.service.id);
        return tokenToMap(t);
    }

    // -------------------------------------------------------------
    // Helper & View Conversion
    // -------------------------------------------------------------

    private double calculateAverageServiceDuration(Long serviceId) {
        var recentLogs = logs.findTop20ByTokenServiceIdOrderByCompletedAtDesc(serviceId);
        if (recentLogs.isEmpty()) {
            return services.findById(serviceId).map(s -> (double) s.defaultMinutes).orElse(5.0);
        }
        return Intelligence.weightedDuration(recentLogs.stream().map(l -> l.durationSeconds).toList(), 5);
    }

    public Map<String, Object> tokenToMap(Models.Token t) {
        var allServiceTokens = tokens.findByServiceIdOrderByIdAsc(t.service.id);
        long ahead = allServiceTokens.stream()
            .filter(x -> "WAITING".equals(x.status) && (x.isPriority == t.isPriority ? x.id < t.id : x.isPriority))
            .count();
        long inService = allServiceTokens.stream()
            .filter(x -> List.of("CALLED", "IN_SERVICE").contains(x.status))
            .count();

        long activeCounters = Math.max(1, counters.findByServiceIdAndActiveTrue(t.service.id).size());
        double avgDuration = calculateAverageServiceDuration(t.service.id);

        var pred = Intelligence.predictWaitTime(
            (int) ahead, (int) inService, (int) activeCounters, avgDuration, t.isPriority, 12.0
        );

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.id);
        m.put("number", t.tokenNumber != null ? t.tokenNumber : "Q" + String.format("%03d", t.id));
        m.put("serviceId", t.service.id);
        m.put("serviceName", t.service.name);
        m.put("status", t.status);
        m.put("isPriority", t.isPriority);
        m.put("priorityReason", t.priorityReason != null ? t.priorityReason : "STANDARD");
        m.put("checkedIn", t.checkedIn);
        m.put("position", "WAITING".equals(t.status) ? ahead + 1 : 0);
        m.put("waitMinutes", "WAITING".equals(t.status) ? pred.estimatedMinutes() : 0);
        m.put("waitRange", pred.lowerBoundMinutes() + " - " + pred.upperBoundMinutes() + " min");
        m.put("explanation", pred.explanationFactors());
        m.put("customerName", t.customer != null ? t.customer.name : "Guest");
        m.put("counterName", t.counter != null ? t.counter.name : "Unassigned");
        m.put("counterId", t.counter != null ? t.counter.id : null);
        m.put("createdAt", t.createdAt != null ? t.createdAt.toString() : "");
        m.put("startedAt", t.startedAt != null ? t.startedAt.toString() : null);
        m.put("completedAt", t.completedAt != null ? t.completedAt.toString() : null);
        return m;
    }

    private void publishQueueUpdate(Long serviceId) {
        ws.convertAndSend("/topic/queue", Map.of(
            "serviceId", serviceId,
            "timestamp", System.currentTimeMillis()
        ));
    }

    private void notifyCustomerTurn(Models.Token t) {
        ws.convertAndSend("/topic/alerts/" + t.customer.id, Map.of(
            "tokenId", t.id,
            "tokenNumber", t.tokenNumber,
            "message", "Now calling " + t.tokenNumber + " at " + (t.counter != null ? t.counter.name : "Service Desk")
        ));
    }

    // -------------------------------------------------------------
    // MODULE 3: Admin Analytics & Reports
    // -------------------------------------------------------------

    @GetMapping("/admin/analytics")
    public Map<String, Object> getAdminAnalytics() {
        var allTokens = tokens.findAll();
        var completed = allTokens.stream().filter(t -> "COMPLETED".equals(t.status)).toList();
        var waiting = allTokens.stream().filter(t -> "WAITING".equals(t.status)).count();
        var inProgress = allTokens.stream().filter(t -> List.of("CALLED", "IN_SERVICE").contains(t.status)).count();

        double avgWaitTime = completed.stream()
            .filter(t -> t.startedAt != null)
            .mapToLong(t -> Duration.between(t.createdAt, t.startedAt).toMinutes())
            .average().orElse(7.5);

        double avgServiceTime = logs.findAll().stream()
            .mapToInt(l -> l.durationSeconds)
            .average().orElse(300) / 60.0;

        var allCounters = counters.findAll();
        long activeCounters = allCounters.stream().filter(c -> c.active).count();
        double utilization = allCounters.isEmpty() ? 0.0 : (double) activeCounters / allCounters.size();

        // Hourly peak distribution
        var peakHours = allTokens.stream().collect(
            Collectors.groupingBy(t -> t.createdAt.getHour(), TreeMap::new, Collectors.counting())
        );

        // Counter efficiency metrics
        var counterStats = allCounters.stream().map(c -> {
            var counterLogs = logs.findByCounterId(c.id);
            double avgMinutes = counterLogs.stream().mapToInt(l -> l.durationSeconds).average().orElse(0) / 60.0;
            Map<String, Object> cm = new HashMap<>();
            cm.put("id", c.id);
            cm.put("name", c.name);
            cm.put("active", c.active);
            cm.put("servedCount", counterLogs.size());
            cm.put("avgDurationMinutes", Intelligence.round(avgMinutes));
            cm.put("serviceName", c.service != null ? c.service.name : "");
            return cm;
        }).toList();

        // Feedback summary
        var allFbs = feedbacks.findAll();
        double avgRating = allFbs.stream().mapToInt(f -> f.rating).average().orElse(4.8);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("totalTokens", allTokens.size());
        res.put("completedTokens", completed.size());
        res.put("waitingTokens", waiting);
        res.put("inProgressTokens", inProgress);
        res.put("activeCounters", activeCounters);
        res.put("totalCounters", allCounters.size());
        res.put("utilizationPercent", Intelligence.round(utilization * 100));
        res.put("avgWaitMinutes", Intelligence.round(avgWaitTime));
        res.put("avgServiceMinutes", Intelligence.round(avgServiceTime));
        res.put("avgCustomerRating", Intelligence.round(avgRating));
        res.put("feedbackCount", allFbs.size());
        res.put("peakHours", peakHours);
        res.put("counterPerformance", counterStats);
        return res;
    }

    @GetMapping(value = "/admin/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv() {
        StringBuilder sb = new StringBuilder("Token ID,Token Number,Customer,Service,Counter,Priority,Status,Created At,Started At,Completed At,Wait Time (min)\n");
        for (var t : tokens.findAllByOrderByIdDesc()) {
            double waitMin = (t.startedAt != null)
                ? Duration.between(t.createdAt, t.startedAt).toSeconds() / 60.0
                : 0.0;
            sb.append(t.id).append(',')
              .append(escapeCsv(t.tokenNumber != null ? t.tokenNumber : "Q" + t.id)).append(',')
              .append(escapeCsv(t.customer != null ? t.customer.name : "Guest")).append(',')
              .append(escapeCsv(t.service != null ? t.service.name : "")).append(',')
              .append(escapeCsv(t.counter != null ? t.counter.name : "Unassigned")).append(',')
              .append(t.isPriority ? "Priority" : "Standard").append(',')
              .append(t.status).append(',')
              .append(t.createdAt != null ? t.createdAt.toString() : "").append(',')
              .append(t.startedAt != null ? t.startedAt.toString() : "").append(',')
              .append(t.completedAt != null ? t.completedAt.toString() : "").append(',')
              .append(Intelligence.round(waitMin)).append('\n');
        }
        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=queueless-audit-report.csv")
            .body(sb.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String escapeCsv(String s) {
        if (s == null) return "\"\"";
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    @GetMapping(value = "/admin/export.pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        try {
            var out = new java.io.ByteArrayOutputStream();
            var doc = new Document();
            PdfWriter.getInstance(doc, out);
            doc.open();

            var titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            var headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLUE);
            var bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            doc.add(new Paragraph("QueueLess — Operational Audit & Queue Performance Report", titleFont));
            doc.add(new Paragraph("Generated at: " + LocalDateTime.now() + "\n\n", bodyFont));

            doc.add(new Paragraph("Summary Statistics:", headerFont));
            doc.add(new Paragraph("• Total Processed Tokens: " + tokens.count(), bodyFont));
            doc.add(new Paragraph("• Total Completed Services: " + logs.count(), bodyFont));
            doc.add(new Paragraph("• Active Service Desks: " + counters.findByActiveTrue().size() + "\n\n", bodyFont));

            doc.add(new Paragraph("Recent Token Activity:", headerFont));
            for (var t : tokens.findAllByOrderByIdDesc().stream().limit(25).toList()) {
                doc.add(new Paragraph(
                    String.format("[%s] %s | Service: %s | Counter: %s | Status: %s",
                        t.tokenNumber != null ? t.tokenNumber : "Q" + t.id,
                        t.customer != null ? t.customer.name : "Guest",
                        t.service != null ? t.service.name : "",
                        t.counter != null ? t.counter.name : "N/A",
                        t.status
                    ), bodyFont
                ));
            }

            doc.close();
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=queueless-summary-report.pdf")
                .body(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    @ExceptionHandler({IllegalArgumentException.class, NoSuchElementException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid request"));
    }
}
