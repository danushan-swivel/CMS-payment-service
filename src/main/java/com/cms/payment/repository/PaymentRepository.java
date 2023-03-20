package com.cms.payment.repository;

import com.cms.payment.domain.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    @Query(value = "SELECT * FROM payment p WHERE p.is_deleted=false", nativeQuery = true)
    Page<Payment> findAll(Pageable pageable);

    @Query(value = "SELECT * FROM payment p WHERE p.is_deleted=false AND p.student_id=?1", nativeQuery = true)
    Page<Payment> findByStudentId(Pageable pageable, String studentId);

    @Query(value = "SELECT * FROM payment p WHERE p.is_deleted=false AND p.payment_id=?1", nativeQuery = true)
    Optional<Payment> findById(String paymentId);

    @Query(value = "SELECT * FROM payment p WHERE p.is_deleted=false AND p.payment_month=?1", nativeQuery = true)
    Page<Payment> findByPaymentMonth(Pageable pageable, String paymentMonth);

    boolean existsByPaymentMonthAndStudentIdAndIsDeletedFalse(String paymentMonth, String studentId);

    boolean existsByPaymentMonthAndStudentIdAndPaymentIdAndIsDeletedFalse(String paymentMonth, String studentId, String paymentId);
}
