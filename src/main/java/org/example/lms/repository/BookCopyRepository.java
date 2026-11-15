
package org.example.lms.repository;

import org.example.lms.entity.BookCopy;
import org.example.lms.entity.BookCopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM BookCopy c WHERE c.id = :id")
    Optional<BookCopy> findByIdForUpdate(@Param("id") Long id);

    long countByStatus(BookCopyStatus status);

    List<BookCopy> findByBook_BookIdOrderByCopyNumberAsc(Long bookId);

    boolean existsByBook_BookIdAndCopyNumber(Long bookId, Integer copyNumber);
}
