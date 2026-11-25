package com.queueless;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDateTime;
import java.time.Duration;

@Configuration
public class Seed {

    @Bean
    public CommandLineRunner initDatabase(
        Repositories.Users users,
        Repositories.Services services,
        Repositories.Counters counters,
        Repositories.Tokens tokens,
        Repositories.Logs logs,
        Repositories.Feedbacks feedbacks,
        PasswordEncoder encoder
    ) {
        return args -> {
            if (users.count() > 0) return; // Prevent duplicate seeding

            // 1. Core Users with BCrypt encrypted passwords
            var admin = users.save(new Models.AppUser("admin@demo.com", "Admin Director", "9876543210", encoder.encode("Admin@123"), "ADMIN"));
            var staff1 = users.save(new Models.AppUser("staff@demo.com", "Alex Staff", "9876543211", encoder.encode("Staff@123"), "STAFF"));
            var staff2 = users.save(new Models.AppUser("staff2@demo.com", "Sarah Staff", "9876543212", encoder.encode("Staff@123"), "STAFF"));
            var customer = users.save(new Models.AppUser("customer@demo.com", "Dharshini V", "9876543213", encoder.encode("Customer@123"), "CUSTOMER"));
            var visitor = users.save(new Models.AppUser("history@demo.com", "Simulated Visitor", "9876543214", encoder.encode("History@123"), "CUSTOMER"));

            // 2. Multi-Domain Services
            var sAccount = services.save(new Models.ServiceType("Account Service", "Account opening, KYC updates, and inquiries", "BANKING", 6));
            var sPayments = services.save(new Models.ServiceType("Payments & Cash", "Deposits, withdrawals, and remittances", "BANKING", 4));
            var sClinic = services.save(new Models.ServiceType("Clinic Consultation", "Outpatient doctor examination and triage", "HEALTHCARE", 8));
            var sDocs = services.save(new Models.ServiceType("Document Verification", "Public services, certificates, and notary", "PUBLIC_ADMIN", 10));

            // 3. Service Counters
            var c1 = counters.save(new Models.Counter("Counter 1 - Accounts", 1, sAccount));
            c1.currentStaff = staff1;
            counters.save(c1);

            var c2 = counters.save(new Models.Counter("Counter 2 - Payments", 2, sPayments));
            c2.currentStaff = staff2;
            counters.save(c2);

            var c3 = counters.save(new Models.Counter("Counter 3 - Clinic Desk", 3, sClinic));
            var c4 = counters.save(new Models.Counter("Counter 4 - Documents", 4, sDocs));
            var c5 = counters.save(new Models.Counter("Counter 5 - Fast Track", 5, sAccount));
            var c6 = counters.save(new Models.Counter("Counter 6 - Inquiry", 6, sPayments));

            // 4. Historical Tokens and Completed Service Logs (for realistic analytics)
            LocalDateTime baseTime = LocalDateTime.now().minusHours(4);
            int[] durations = { 240, 310, 180, 420, 290, 350, 210, 480, 270, 330, 260, 390, 220, 310, 450, 280, 300, 360 };

            for (int i = 0; i < durations.length; i++) {
                var s = (i % 2 == 0) ? sAccount : sPayments;
                var c = (i % 2 == 0) ? c1 : c2;
                var t = new Models.Token(visitor, s, i == 3 || i == 9, (i == 3 || i == 9) ? "SENIOR" : "STANDARD");
                t.tokenNumber = String.format("%c%03d", s.name.charAt(0), i + 1);
                t.createdAt = baseTime.plusMinutes(i * 12);
                t.startedAt = t.createdAt.plusMinutes(5 + (i % 4));
                t.completedAt = t.startedAt.plusSeconds(durations[i]);
                t.counter = c;
                t.status = "COMPLETED";
                t.checkedIn = true;
                t.checkedInAt = t.createdAt.plusMinutes(2);
                tokens.save(t);

                logs.save(new Models.ServiceLog(
                    t, c, staff1, durations[i], (int) Duration.between(t.createdAt, t.startedAt).getSeconds()
                ));

                if (i % 3 == 0) {
                    feedbacks.save(new Models.Feedback(t, visitor, 4 + (i % 2), "Smooth service and accurate waiting time prediction!"));
                }
            }

            // 5. Active Waiting Queue for Demo Walkthrough
            var tNow1 = new Models.Token(customer, sAccount);
            tNow1.tokenNumber = "A045";
            tNow1.createdAt = LocalDateTime.now().minusMinutes(12);
            tNow1.status = "WAITING";
            tNow1.checkedIn = true;
            tNow1.checkedInAt = tNow1.createdAt.plusMinutes(1);
            tokens.save(tNow1);

            var tNow2 = new Models.Token(visitor, sAccount, true, "SENIOR_CITIZEN");
            tNow2.tokenNumber = "A046";
            tNow2.createdAt = LocalDateTime.now().minusMinutes(8);
            tNow2.status = "WAITING";
            tokens.save(tNow2);

            var tNow3 = new Models.Token(visitor, sAccount);
            tNow3.tokenNumber = "A047";
            tNow3.createdAt = LocalDateTime.now().minusMinutes(4);
            tNow3.status = "WAITING";
            tokens.save(tNow3);

            var tCalled = new Models.Token(visitor, sAccount);
            tCalled.tokenNumber = "A039";
            tCalled.createdAt = LocalDateTime.now().minusMinutes(20);
            tCalled.startedAt = LocalDateTime.now().minusMinutes(3);
            tCalled.counter = c1;
            tCalled.status = "CALLED";
            tokens.save(tCalled);
        };
    }
}
