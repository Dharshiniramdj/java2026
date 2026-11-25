package com.queueless;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface Repositories {

    interface Users extends JpaRepository<Models.AppUser, Long> {
        Optional<Models.AppUser> findByEmail(String email);
        List<Models.AppUser> findByRole(String role);
    }

    interface Services extends JpaRepository<Models.ServiceType, Long> {
        List<Models.ServiceType> findByActiveTrue();
    }

    interface Counters extends JpaRepository<Models.Counter, Long> {
        List<Models.Counter> findByServiceId(Long serviceId);
        List<Models.Counter> findByActiveTrue();
        List<Models.Counter> findByServiceIdAndActiveTrue(Long serviceId);
    }

    interface Tokens extends JpaRepository<Models.Token, Long> {
        List<Models.Token> findByServiceIdOrderByIdAsc(Long serviceId);
        List<Models.Token> findByCustomerIdOrderByIdDesc(Long customerId);
        List<Models.Token> findAllByOrderByIdDesc();
        List<Models.Token> findByStatus(String status);
        List<Models.Token> findByServiceIdAndStatus(Long serviceId, String status);

        @Query("SELECT t FROM Models$Token t WHERE t.service.id = :serviceId AND t.status IN ('WAITING', 'CALLED', 'IN_SERVICE') ORDER BY t.isPriority DESC, t.id ASC")
        List<Models.Token> findActiveQueue(@Param("serviceId") Long serviceId);

        Optional<Models.Token> findByQrSecret(String qrSecret);
    }

    interface Logs extends JpaRepository<Models.ServiceLog, Long> {
        List<Models.ServiceLog> findTop20ByTokenServiceIdOrderByCompletedAtDesc(Long serviceId);
        List<Models.ServiceLog> findTop50ByTokenServiceIdOrderByCompletedAtDesc(Long serviceId);
        List<Models.ServiceLog> findByCounterId(Long counterId);
        List<Models.ServiceLog> findAllByOrderByCompletedAtDesc();
    }

    interface Feedbacks extends JpaRepository<Models.Feedback, Long> {
        List<Models.Feedback> findAllByOrderByCreatedAtDesc();
        Optional<Models.Feedback> findByTokenId(Long tokenId);
    }
}
