
package org.example.lms.repository;

import org.example.lms.entity.Fine;
import org.example.lms.entity.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long> {

    Optional<Fine> findByBorrowing_Id(Long borrowingId);

    List<Fine> findByStatus(FineStatus status);

    List<Fine> findByBorrowing_Patron_EmailOrderByCreatedAtDesc(
            String email
    );

    long countByStatus(FineStatus status);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f")
    BigDecimal sumAllFineAmounts();

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.status = :status")
    BigDecimal sumFineAmountsByStatus(@Param("status") FineStatus status);

    long countByBorrowing_Patron_EmailAndStatus(
            String email,
            FineStatus status
    );

    List<Fine> findByBorrowing_Patron_IdOrderByCreatedAtDesc(Long patronId);
}
