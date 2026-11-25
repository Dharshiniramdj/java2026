package com.queueless;

import jakarta.persistence.*;
import java.time.LocalDateTime;

public class Models {

    @Entity
    @Table(name = "app_users")
    public static class AppUser {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        @Column(unique = true, nullable = false)
        public String email;

        public String name;
        public String phone;
        public String passwordHash;
        public String role; // CUSTOMER, STAFF, ADMIN
        public LocalDateTime createdAt = LocalDateTime.now();

        protected AppUser() {}

        public AppUser(String email, String name, String passwordHash, String role) {
            this.email = email;
            this.name = name;
            this.passwordHash = passwordHash;
            this.role = role;
        }

        public AppUser(String email, String name, String phone, String passwordHash, String role) {
            this.email = email;
            this.name = name;
            this.phone = phone;
            this.passwordHash = passwordHash;
            this.role = role;
        }
    }

    @Entity
    @Table(name = "service_types")
    public static class ServiceType {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        public String name;
        public String description;
        public String category; // BANKING, HEALTHCARE, PUBLIC_ADMIN
        public int defaultMinutes = 5;
        public boolean active = true;

        protected ServiceType() {}

        public ServiceType(String name, int defaultMinutes) {
            this.name = name;
            this.defaultMinutes = defaultMinutes;
            this.category = "GENERAL";
        }

        public ServiceType(String name, String description, String category, int defaultMinutes) {
            this.name = name;
            this.description = description;
            this.category = category;
            this.defaultMinutes = defaultMinutes;
            this.active = true;
        }
    }

    @Entity
    @Table(name = "service_counters")
    public static class Counter {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        public String name;
        public int counterNumber;
        public boolean active = true;

        @ManyToOne
        public ServiceType service;

        @ManyToOne
        public AppUser currentStaff;

        protected Counter() {}

        public Counter(String name, ServiceType service) {
            this.name = name;
            this.service = service;
            this.active = true;
        }

        public Counter(String name, int counterNumber, ServiceType service) {
            this.name = name;
            this.counterNumber = counterNumber;
            this.service = service;
            this.active = true;
        }
    }

    @Entity
    @Table(name = "queue_tokens", indexes = {
        @Index(name = "idx_token_created_at", columnList = "createdAt"),
        @Index(name = "idx_token_status", columnList = "status")
    })
    public static class Token {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        public String tokenNumber; // e.g. A015, B008

        @ManyToOne
        public AppUser customer;

        @ManyToOne
        public ServiceType service;

        @ManyToOne
        public Counter counter;

        public String status = "WAITING"; // WAITING, CALLED, IN_SERVICE, COMPLETED, SKIPPED, CANCELLED

        public boolean isPriority = false;
        public String priorityReason; // SENIOR, MEDICAL, VIP, STANDARD

        public LocalDateTime createdAt = LocalDateTime.now();
        public LocalDateTime startedAt;
        public LocalDateTime completedAt;

        @Column(unique = true)
        public String qrSecret = java.util.UUID.randomUUID().toString();

        public boolean notified = false;
        public boolean checkedIn = false;
        public LocalDateTime checkedInAt;

        public int estimatedWaitMinutes = 0;

        protected Token() {}

        public Token(AppUser customer, ServiceType service) {
            this.customer = customer;
            this.service = service;
            this.status = "WAITING";
            this.isPriority = false;
        }

        public Token(AppUser customer, ServiceType service, boolean isPriority, String priorityReason) {
            this.customer = customer;
            this.service = service;
            this.isPriority = isPriority;
            this.priorityReason = priorityReason;
            this.status = "WAITING";
        }
    }

    @Entity
    @Table(name = "service_logs")
    public static class ServiceLog {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        @ManyToOne
        public Token token;

        @ManyToOne
        public Counter counter;

        @ManyToOne
        public AppUser staff;

        public int durationSeconds;
        public int waitDurationSeconds;
        public LocalDateTime completedAt = LocalDateTime.now();

        protected ServiceLog() {}

        public ServiceLog(Token token, Counter counter, int durationSeconds) {
            this.token = token;
            this.counter = counter;
            this.durationSeconds = durationSeconds;
            this.completedAt = LocalDateTime.now();
        }

        public ServiceLog(Token token, Counter counter, AppUser staff, int durationSeconds, int waitDurationSeconds) {
            this.token = token;
            this.counter = counter;
            this.staff = staff;
            this.durationSeconds = durationSeconds;
            this.waitDurationSeconds = waitDurationSeconds;
            this.completedAt = LocalDateTime.now();
        }
    }

    @Entity
    @Table(name = "customer_feedback")
    public static class Feedback {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long id;

        @ManyToOne
        public Token token;

        @ManyToOne
        public AppUser customer;

        public int rating; // 1 to 5 stars
        public String comments;
        public LocalDateTime createdAt = LocalDateTime.now();

        protected Feedback() {}

        public Feedback(Token token, AppUser customer, int rating, String comments) {
            this.token = token;
            this.customer = customer;
            this.rating = rating;
            this.comments = comments;
            this.createdAt = LocalDateTime.now();
        }
    }
}
